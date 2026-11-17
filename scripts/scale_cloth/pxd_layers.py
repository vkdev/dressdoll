"""Извлекает слои из Pixelmator Pro .pxd: PNG каждого слоя и его позицию на холсте в пикселях.

Использование: python3 pxd_layers.py <файл.pxd> [папка_вывода]
По умолчанию вывод в <имя>_layers рядом с .pxd. Формат слоёв — в README.md рядом.
"""
import io
import json
import sqlite3
import struct
import sys
import tempfile
import zipfile
import zlib
from pathlib import Path

import numpy as np
from PIL import Image

BITMAP_MAGIC = b"PTBitmapBuffer__"


def decode(blob):
    # Значения в layer_info: магия «4-tP», тип из 4 символов (байты в обратном порядке),
    # длина полезной части (uint32 LE), затем big-endian double.
    tag = blob[4:8][::-1].decode()
    n = struct.unpack("<I", blob[8:12])[0]
    payload = blob[12:12 + n]
    if tag == "Strn":
        length = struct.unpack("<I", payload[:4])[0]
        return payload[4:4 + length].decode()
    return struct.unpack(">%dd" % (n // 8), payload)


def decode_bitmap(blob):
    # Растровый слой (нарисованный в Pixelmator) лежит в data/<id> без расширения.
    # Заголовок: магия, версия, дата (double), ширина, высота, байт в строке, ещё два uint32,
    # 8 байт 0xFF, затем блок формата «4-tP…» переменной длины; дальше raw deflate.
    # Пиксели — BGRA с предумноженной альфой, строки сверху вниз (выяснено на doll2_test).
    if blob[:16] != BITMAP_MAGIC:
        raise ValueError("неизвестный формат растрового слоя")
    width, height, row_bytes = struct.unpack("<3I", blob[28:40])
    fmt_len = struct.unpack("<I", blob[64:68])[0]
    raw = zlib.decompressobj(-15).decompress(blob[68 + fmt_len:])
    px = np.frombuffer(raw, np.uint8).reshape(height, row_bytes)[:, :width * 4].reshape(height, width, 4)
    rgba = px[..., [2, 1, 0, 3]].astype(np.float32)
    alpha = rgba[..., 3:4]
    rgba[..., :3] = np.where(alpha > 0, rgba[..., :3] * 255.0 / np.maximum(alpha, 1), 0)
    return Image.fromarray(np.clip(rgba + 0.5, 0, 255).astype(np.uint8), "RGBA")


def extract(pxd_path, out_dir):
    out_dir.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(pxd_path) as z, tempfile.TemporaryDirectory() as tmp:
        db_path = Path(tmp) / "metadata.info"
        db_path.write_bytes(z.read("metadata.info"))
        db = sqlite3.connect(db_path)

        size_blob = db.execute("select value from document_info where key='size'").fetchone()[0]
        canvas_w, _, canvas_h, _ = struct.unpack("<4i", size_blob[12:28])

        layers = []
        rows = db.execute(
            "select id, identifier, index_at_parent from document_layers "
            "where parent_identifier is null order by index_at_parent desc"
        ).fetchall()
        # index_at_parent = 0 — верхний слой, поэтому сортируем по убыванию: снизу вверх.
        for order, (lid, ident, _) in enumerate(rows):
            info = {k: v for k, v in db.execute("select key, value from layer_info where layer_id=?", (lid,))}
            name = decode(info["name"])
            scale = decode(info["backingScale"])[0]
            cx, cy = decode(info["position"])
            w_pt, h_pt = decode(info["size"])
            transform = decode(info["transform"])
            if transform != (1.0, 0.0, 0.0, 1.0, 0.0, 0.0) or decode(info["angle"])[0] != 0:
                print(f"! слой «{name}»: есть трансформация/поворот, позиция может быть неточной", file=sys.stderr)
            source_name = f"data/{ident}-OriginalContentSource"
            is_bitmap = source_name not in z.namelist()
            if is_bitmap:
                content = decode_bitmap(z.read(f"data/{ident}"))
            else:
                if db.execute("select count(*) from layer_tiles where layer_id=?", (lid,)).fetchone()[0]:
                    print(f"! слой «{name}»: есть правки пикселей (layer_tiles), они не учитываются", file=sys.stderr)
                src = Image.open(io.BytesIO(z.read(source_name))).convert("RGBA")
                # Для картинки Pixelmator хранит размер слоя по непрозрачному содержимому.
                content = src.crop(src.getchannel("A").getbbox())

            # Растровый слой, уменьшенный в Pixelmator, хранит пиксели в исходном размере,
            # а уменьшение — отдельно в «scale». Масштабирование идёт вокруг anchorPoint.
            sx, sy = decode(info["scale"]) if "scale" in info else (1.0, 1.0)
            if "anchorPoint" in info and decode(info["anchorPoint"]) != (0.5, 0.5) and (sx, sy) != (1.0, 1.0):
                print(f"! слой «{name}»: anchorPoint не в центре, позиция может быть неточной", file=sys.stderr)
            w_pt, h_pt = w_pt * sx, h_pt * sy

            # position — центр слоя в точках, ось Y снизу вверх (Cocoa).
            w_px, h_px = round(w_pt * scale), round(h_pt * scale)
            left = round(cx * scale - w_px / 2)
            top = round((canvas_h / scale - cy) * scale - h_px / 2)
            if content.size != (w_px, h_px):
                content = content.resize((w_px, h_px), Image.LANCZOS)
            if is_bitmap:
                # У растрового слоя size — весь буфер, прозрачные поля срезаем уже после расчёта позиции.
                bbox = content.getchannel("A").getbbox()
                content = content.crop(bbox)
                left, top = left + bbox[0], top + bbox[1]
                w_px, h_px = content.size

            file_name = f"{order:02d}_{name}.png"
            content.save(out_dir / file_name)
            layers.append({
                "order": order, "name": name, "file": file_name,
                "x": left, "y": top, "width": w_px, "height": h_px,
            })

    meta = {"canvas": {"width": canvas_w, "height": canvas_h}, "layers": layers}
    (out_dir / "layers.json").write_text(json.dumps(meta, ensure_ascii=False, indent=2))

    preview = Image.new("RGBA", (canvas_w, canvas_h), (255, 255, 255, 255))
    for layer in layers:
        img = Image.open(out_dir / layer["file"])
        preview.alpha_composite(img, (layer["x"], layer["y"]))
    preview.save(out_dir / "_preview.png")
    return meta


if __name__ == "__main__":
    pxd = Path(sys.argv[1])
    out = Path(sys.argv[2]) if len(sys.argv) > 2 else pxd.with_name(pxd.stem + "_layers")
    print(json.dumps(extract(pxd, out), ensure_ascii=False, indent=2))
