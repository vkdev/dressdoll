package ru.vkdev.dressdoll.buildlogic.clothing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class DollPlacementValidatorTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `корректная вещь даёт позицию`() {
        val result = validate()

        assertEquals(emptyList<String>(), result.errors)
        assertEquals(
            DollConfig(CANVAS, FIGURE, listOf(ItemPlacement("блузка", "01", x = 215, y = 266))),
            result.config,
        )
    }

    @Test
    fun `настоящий датасет проходит без ошибок`() {
        // Тесты build-logic запускаются из его каталога, датасет — соседний модуль.
        val assets = File("../temporary-dataset/src/main/assets")
        val clothSrc = assets.resolve("cloth-src")
        val image = ImageIO.read(assets.resolve("doll_1.png"))
        val entries = ClothSrcEntries.scan(clothSrc)

        val result = DollPlacementValidator.validate(
            canvas = ImageSize(image.width, image.height),
            figure = DollPlacementValidator.figureBounds(image),
            items = entries.items,
            scaleJson = { clothSrc.resolve(it.path).resolve(ItemFiles.SCALE).readText() },
        )

        assertEquals(emptyList<String>(), result.errors)
        assertEquals(entries.items.size, result.config!!.placements.size)
        assertEquals(CanvasRect(left = 221, top = 66, width = 329, height = 1226), result.config!!.figure)
    }

    @Test
    fun `нет обязательных файлов`() {
        val errors = validate(item = item(files = listOf(ItemFiles.BACK))).errors

        assertEquals(
            listOf("«блузка/01»: нет scale.json", "«блузка/01»: нет front.png", "«блузка/01»: нет catalog.png"),
            errors,
        )
    }

    @Test
    fun `back необязателен`() {
        val result = validate(item = item(files = FILES - ItemFiles.BACK, sizes = mapOf(ItemFiles.FRONT to FRONT)))

        assertEquals(emptyList<String>(), result.errors)
    }

    @Test
    fun `холст в scale json не совпадает с куклой`() {
        val errors = validate(scale = SCALE.replace("[768, 1376]", "[768, 1400]")).errors

        assertEquals(listOf("«блузка/01»: doll_canvas 768×1400, а doll_1.png 768×1376"), errors)
    }

    @Test
    fun `scaled_size не совпадает с front`() {
        val errors = validate(scale = SCALE.replace("[342, 436]", "[342, 435]")).errors

        assertEquals(listOf("«блузка/01»: scaled_size 342×435, а front.png 342×436"), errors)
    }

    @Test
    fun `back другого размера`() {
        val errors = validate(item = item(sizes = mapOf(ItemFiles.FRONT to FRONT, ItemFiles.BACK to ImageSize(342, 400)))).errors

        assertEquals(
            listOf("«блузка/01»: back.png 342×400, а front.png 342×436: плоскости ставятся в одну точку и должны совпадать"),
            errors,
        )
    }

    @Test
    fun `offset не из целых чисел`() {
        val errors = validate(scale = SCALE.replace("[215, 266]", "[215.5, 266]")).errors

        assertEquals(1, errors.size)
        assertTrue(errors.single(), errors.single().startsWith("«блузка/01»: «offset_on_doll» в scale.json должен быть массивом из двух целых"))
    }

    @Test
    fun `offset без второй координаты`() {
        val errors = validate(scale = SCALE.replace("[215, 266]", "[215]")).errors

        assertEquals(1, errors.size)
        assertTrue(errors.single().contains("«offset_on_doll»"))
    }

    @Test
    fun `вещь целиком за холстом`() {
        val errors = validate(scale = SCALE.replace("[215, 266]", "[-342, 266]")).errors

        assertEquals(listOf("«блузка/01»: offset_on_doll [-342, 266] уводит вещь 342×436 целиком за холст 768×1376"), errors)
    }

    @Test
    fun `вещь частично за холстом допустима`() {
        assertEquals(emptyList<String>(), validate(scale = SCALE.replace("[215, 266]", "[-341, -435]")).errors)
        assertEquals(emptyList<String>(), validate(scale = SCALE.replace("[215, 266]", "[767, 1375]")).errors)
    }

    @Test
    fun `битый scale json`() {
        val errors = validate(scale = "{ \"offset_on_doll\": [").errors

        assertEquals(1, errors.size)
        assertTrue(errors.single(), errors.single().startsWith("«блузка/01»: не удалось разобрать scale.json"))
    }

    @Test
    fun `front не PNG`() {
        val errors = validate(item = item(sizes = mapOf(ItemFiles.FRONT to null, ItemFiles.BACK to FRONT))).errors

        assertEquals(listOf("«блузка/01»: front.png не читается как PNG"), errors)
    }

    @Test
    fun `файл в папке категории`() {
        val errors = validate(items = listOf(item(), ClothSrcItem.strayFile("блузка", "notes.txt"))).errors

        assertEquals(listOf("файл «блузка/notes.txt» лежит в папке категории: там должны быть только папки вещей"), errors)
    }

    @Test
    fun `пустая картинка куклы`() {
        val errors = validate(figure = null).errors

        assertEquals(listOf("doll_1.png: нет ни одного пикселя с альфой выше 12"), errors)
    }

    @Test
    fun `все нарушения попадают в отчёт вместе`() {
        val broken = item(folder = "02", files = FILES - ItemFiles.CATALOG)
        val scales = mapOf(
            "01" to SCALE.replace("[768, 1376]", "[1, 1]"),
            "02" to SCALE.replace("[342, 436]", "[1, 1]"),
        )

        val errors = DollPlacementValidator.validate(CANVAS, figure = null, items = listOf(item(), broken)) {
            scales.getValue(it.folder)
        }.errors

        assertEquals(4, errors.size)
        assertTrue(errors.any { it.startsWith("doll_1.png") })
        assertTrue(errors.any { it.startsWith("«блузка/01»: doll_canvas") })
        assertTrue(errors.any { it == "«блузка/02»: нет catalog.png" })
        assertTrue(errors.any { it.startsWith("«блузка/02»: scaled_size") })
    }

    @Test
    fun `прямоугольник фигурки по альфе выше порога`() {
        val image = BufferedImage(10, 20, BufferedImage.TYPE_INT_ARGB)
        image.setRGB(2, 3, 0x0D000000) // альфа 13 — выше порога
        image.setRGB(7, 15, 0xFF000000.toInt())
        image.setRGB(9, 19, 0x0C000000) // альфа 12 — ореол, не фигурка

        assertEquals(CanvasRect(left = 2, top = 3, width = 6, height = 13), DollPlacementValidator.figureBounds(image))
    }

    @Test
    fun `у прозрачной картинки фигурки нет`() {
        assertNull(DollPlacementValidator.figureBounds(BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB)))
    }

    @Test
    fun `scan видит папки вещей, их файлы и размеры`() {
        val root = tmp.newFolder("cloth-src")
        val item = root.resolve("блузка/01").apply { mkdirs() }
        png(item.resolve(ItemFiles.FRONT), 5, 7)
        item.resolve(ItemFiles.BACK).writeText("не картинка")
        item.resolve(ItemFiles.SCALE).writeText("{}")
        item.resolve(".DS_Store").writeText("")
        root.resolve("блузка/_draft").mkdirs()
        root.resolve("блузка/.DS_Store").writeText("")

        val entries = ClothSrcEntries.scan(root)

        assertEquals(
            listOf(
                ClothSrcItem(
                    category = "блузка",
                    folder = "01",
                    isFolder = true,
                    files = listOf(ItemFiles.BACK, ItemFiles.FRONT, ItemFiles.SCALE),
                    imageSizes = mapOf(ItemFiles.BACK to null, ItemFiles.FRONT to ImageSize(5, 7)),
                ),
            ),
            entries.items,
        )
    }

    @Test
    fun `scan отмечает файл в папке категории`() {
        val root = tmp.newFolder("cloth-src")
        root.resolve("блузка").mkdirs()
        root.resolve("блузка/notes.txt").writeText("")

        assertEquals(listOf(ClothSrcItem.strayFile("блузка", "notes.txt")), ClothSrcEntries.scan(root).items)
    }

    private fun validate(
        item: ClothSrcItem = item(),
        items: List<ClothSrcItem> = listOf(item),
        scale: String = SCALE,
        figure: CanvasRect? = FIGURE,
    ): DollValidationResult = DollPlacementValidator.validate(CANVAS, figure, items) { scale }

    private fun png(file: File, width: Int, height: Int) {
        ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png", file)
    }

    private companion object {
        val CANVAS = ImageSize(768, 1376)
        val FIGURE = CanvasRect(left = 221, top = 66, width = 329, height = 1226)
        val FRONT = ImageSize(342, 436)
        val FILES = listOf(ItemFiles.BACK, ItemFiles.CATALOG, ItemFiles.FRONT, ItemFiles.SCALE)

        fun item(
            folder: String = "01",
            files: List<String> = FILES,
            sizes: Map<String, ImageSize?> = mapOf(ItemFiles.FRONT to FRONT, ItemFiles.BACK to FRONT),
        ) = ClothSrcItem("блузка", folder, isFolder = true, files = files, imageSizes = sizes.filterKeys { it in files })

        // Поля, которые приложение не читает, остаются: проверка их не трогает.
        val SCALE = """
            {
              "s_x": 0.48,
              "scaled_size": [342, 436],
              "doll_canvas": [768, 1376],
              "offset_on_doll": [215, 266]
            }
        """.trimIndent()
    }
}
