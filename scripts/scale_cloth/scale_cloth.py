"""Масштабирует front.png/back.png одежды под размер куклы по второму слою ondoll.pxd.

Подробности — в README.md рядом.
"""
import argparse
import json
import sys
import tempfile
from pathlib import Path

from PIL import Image

from pxd_layers import extract

ASPECT_TOLERANCE = 0.01
# У слоя в pxd край мягче, чем у отмасштабированного front, — по порогу альфы он на пиксель
# уже с каждой стороны.
SIZE_TOLERANCE_PX = 2
CHECK_ALPHA = 32
DEFAULT_ROOT = Path(__file__).resolve().parents[2] / "cloth-src"


def opaque_bbox(img):
    return img.getchannel("A").getbbox()


def visible_size(img):
    b = img.getchannel("A").point(lambda a: 255 if a > CHECK_ALPHA else 0).getbbox()
    return b[2] - b[0], b[3] - b[1]


def scale_folder(folder):
    warnings = []
    with tempfile.TemporaryDirectory() as tmp:
        meta = extract(folder / "ondoll.pxd", Path(tmp))
        if len(meta["layers"]) < 2:
            raise ValueError(f"{folder / 'ondoll.pxd'}: меньше двух слоёв")
        doll_layer, cloth_layer = meta["layers"][0], meta["layers"][1]
        doll_img = Image.open(Path(tmp) / doll_layer["file"]).convert("RGBA")
        cloth_img = Image.open(Path(tmp) / cloth_layer["file"]).convert("RGBA")

    front = Image.open(folder / "front.png").convert("RGBA")
    bbox = opaque_bbox(front)
    if bbox is None:
        raise ValueError(f"{folder / 'front.png'}: изображение полностью прозрачное")
    x, y = bbox[0], bbox[1]
    w, h = bbox[2] - bbox[0], bbox[3] - bbox[1]
    lw, lh = cloth_layer["width"], cloth_layer["height"]
    lx, ly = cloth_layer["x"], cloth_layer["y"]

    s_x, s_y = lw / w, lh / h
    if abs(s_x - s_y) / max(s_x, s_y) > ASPECT_TOLERANCE:
        warnings.append(f"s_x={s_x:.4f} и s_y={s_y:.4f} различаются на "
                        f"{abs(s_x - s_y) / max(s_x, s_y):.1%}: слой растянут непропорционально?")

    W, H = front.size
    scaled_size = (round(W * s_x), round(H * s_y))
    # Pillow ресайзит RGBA через предумноженную альфу (RGBa), поэтому тёмного ореола на краях нет.
    front_scaled = front.resize(scaled_size, Image.LANCZOS)
    front_scaled.save(folder / "front_scaled.png")

    back_path = folder / "back.png"
    if back_path.exists():
        back = Image.open(back_path).convert("RGBA")
        if back.size != front.size:
            warnings.append(f"размер back.png {back.size} не совпадает с front.png {front.size}")
        back.resize((round(back.width * s_x), round(back.height * s_y)), Image.LANCZOS).save(
            folder / "back_scaled.png")

    # Края после ресайза и у самого слоя размыты на пару почти прозрачных пикселей,
    # поэтому сравниваем обе картинки по заметной альфе.
    sw, sh = visible_size(front_scaled)
    vw, vh = visible_size(cloth_img)
    if abs(sw - vw) > SIZE_TOLERANCE_PX or abs(sh - vh) > SIZE_TOLERANCE_PX:
        warnings.append(f"после ресайза содержимое {sw}×{sh}, а слой в pxd {vw}×{vh} (alpha > {CHECK_ALPHA})")

    offset = (round(lx - x * s_x), round(ly - y * s_y))

    canvas = meta["canvas"]
    check = Image.new("RGBA", (canvas["width"], canvas["height"]), (255, 255, 255, 255))
    check.alpha_composite(doll_img, (doll_layer["x"], doll_layer["y"]))
    check.alpha_composite(front_scaled, offset)
    check.save(folder / "_check.png")

    result = {
        "s_x": s_x, "s_y": s_y,
        "source_size": [W, H], "scaled_size": list(scaled_size),
        "front_bbox": [x, y, w, h], "layer_rect": [lx, ly, lw, lh],
        "doll_canvas": [canvas["width"], canvas["height"]], "offset_on_doll": list(offset),
    }
    (folder / "scale.json").write_text(json.dumps(result, ensure_ascii=False, indent=2))
    return result, warnings


def find_folders(root, only):
    if only:
        return [root / only]
    if (root / "ondoll.pxd").exists():
        return [root]
    return sorted({p.parent for p in root.rglob("ondoll.pxd")})


def main():
    parser = argparse.ArgumentParser(description="Масштабирует одежду под куклу по ondoll.pxd.")
    parser.add_argument("root", nargs="?", default=DEFAULT_ROOT, type=Path,
                        help="cloth-src целиком или одна папка с ondoll.pxd (по умолчанию cloth-src репозитория)")
    parser.add_argument("--only", metavar="<тип>/<NN>", help="обработать одну папку внутри root")
    args = parser.parse_args()

    done, skipped, warned = 0, 0, 0
    for folder in find_folders(args.root, args.only):
        missing = [n for n in ("ondoll.pxd", "front.png") if not (folder / n).exists()]
        if missing:
            print(f"- {folder}: пропуск, нет {', '.join(missing)}")
            skipped += 1
            continue
        try:
            result, warnings = scale_folder(folder)
        except ValueError as e:
            print(f"! {folder}: {e}")
            skipped += 1
            continue
        done += 1
        print(f"+ {folder}: s={result['s_x']:.4f}×{result['s_y']:.4f}, "
              f"{result['source_size']} → {result['scaled_size']}, offset {result['offset_on_doll']}")
        for w in warnings:
            print(f"  ! {w}")
        warned += bool(warnings)
    print(f"\nОбработано: {done}, пропущено: {skipped}, с предупреждениями: {warned}")
    return 0 if skipped == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
