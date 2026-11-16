package ru.vkdev.dressdoll.buildlogic.clothing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClothingDatasetValidatorTest {

    @Test
    fun `корректный конфиг проходит без ошибок`() {
        assertEquals(emptyList<String>(), validate())
    }

    @Test
    fun `настоящий конфиг датасета согласован с папками`() {
        // Тесты build-logic запускаются из его каталога, датасет — соседний модуль.
        val assets = File("../temporary-dataset/src/main/assets")
        val folders = assets.resolve("cloth-src").listFiles().orEmpty()
            .filter { it.isDirectory }
            .mapTo(mutableSetOf()) { it.name }

        val errors = ClothingDatasetValidator.validate(assets.resolve("categories.json").readText(), folders)

        assertEquals(emptyList<String>(), errors)
    }

    @Test
    fun `папка без записи`() {
        val errors = validate(folders = FOLDERS + "галстук-бабочка")

        assertEquals(listOf("папка «галстук-бабочка» есть, записи в categories нет"), errors)
    }

    @Test
    fun `запись без папки`() {
        val errors = validate(folders = FOLDERS - "трусы")

        assertEquals(listOf("запись «трусы» есть, папки в cloth-src нет"), errors)
    }

    @Test
    fun `неизвестный слот`() {
        val errors = validate(config = CONFIG.replace("\"slot\": \"feet\"", "\"slot\": \"foot\""))

        assertEquals(1, errors.size)
        assertTrue(errors.single(), errors.single().startsWith("«носки»: слот «foot» не найден"))
    }

    @Test
    fun `неизвестный уровень`() {
        val errors = validate(config = CONFIG.replace("\"level\": \"socks\"", "\"level\": \"sokcs\""))

        assertEquals(
            listOf(
                "«носки»: уровень «sokcs» не найден",
                "уровень «socks» не используется ни одной категорией",
            ),
            errors,
        )
    }

    @Test
    fun `неизвестная группа`() {
        val errors = validate(config = CONFIG.replace("\"group\": \"socks\"", "\"group\": \"sock\""))

        assertEquals(
            listOf(
                "«носки»: группа «sock» не найдена",
                "группа «socks» не используется ни одной категорией",
            ),
            errors,
        )
    }

    @Test
    fun `неизвестный раздел`() {
        val errors = validate(config = CONFIG.replace("\"section\": \"hosiery\"", "\"section\": \"hosery\""))

        assertEquals(
            listOf(
                "«носки»: раздел «hosery» не найден",
                "раздел «hosiery» пуст",
            ),
            errors,
        )
    }

    @Test
    fun `пустой раздел`() {
        val config = CONFIG.replace(
            "{ \"id\": \"hosiery\", \"title\": \"Носки\" }",
            "{ \"id\": \"hosiery\", \"title\": \"Носки\" }, { \"id\": \"shoes\", \"title\": \"Обувь\" }",
        )

        assertEquals(listOf("раздел «shoes» пуст"), validate(config = config))
    }

    @Test
    fun `нулевой лимит группы`() {
        val errors = validate(config = CONFIG.replace("\"limit\": 1", "\"limit\": 0"))

        assertEquals(listOf("группа «socks»: limit должен быть целым числом ≥ 1, сейчас «0»"), errors)
    }

    @Test
    fun `дубликат уровня`() {
        val errors = validate(config = CONFIG.replace("[\"underwear\", \"socks\"]", "[\"underwear\", \"socks\", \"socks\"]"))

        assertEquals(listOf("уровень «socks» объявлен несколько раз"), errors)
    }

    @Test
    fun `имя папки в NFD`() {
        // «й» как «и» + комбинируемая бревис U+0306.
        val nfd = "майка"
        val nfc = "майка"
        val config = CONFIG.replace("\"трусы\"", "\"$nfc\"")

        val errors = validate(config = config, folders = FOLDERS - "трусы" + nfd)

        assertEquals(listOf("имя папки «$nfd» не в NFC"), errors)
    }

    @Test
    fun `ключ категории в NFD`() {
        val nfd = "майка"
        val nfc = "майка"
        val config = CONFIG.replace("\"трусы\"", "\"$nfd\"")

        val errors = validate(config = config, folders = FOLDERS - "трусы" + nfc)

        assertEquals(listOf("ключ категории «$nfd» не в NFC"), errors)
    }

    @Test
    fun `лишнее поле категории`() {
        val errors = validate(config = CONFIG.replace("\"level\": \"socks\"", "\"level\": \"socks\", \"levle\": \"socks\""))

        assertEquals(listOf("«носки»: лишнее поле «levle»"), errors)
    }

    @Test
    fun `неявная группа совпадает с явной`() {
        val config = CONFIG.replace("\"socks\": { \"limit\": 1 }", "\"socks\": { \"limit\": 1 }, \"трусы\": { \"limit\": 1 }")

        assertEquals(
            listOf(
                "«трусы»: неявная группа совпадает с явной группой «трусы»",
                "группа «трусы» не используется ни одной категорией",
            ),
            validate(config = config),
        )
    }

    @Test
    fun `битый JSON`() {
        val errors = validate(config = "{ \"levels\": [")

        assertEquals(1, errors.size)
        assertTrue(errors.single(), errors.single().startsWith("не удалось разобрать JSON"))
    }

    @Test
    fun `все нарушения попадают в отчёт вместе`() {
        val config = CONFIG
            .replace("\"slot\": \"feet\"", "\"slot\": \"foot\"")
            .replace("\"limit\": 1", "\"limit\": 0")

        val errors = validate(config = config, folders = FOLDERS + "галстук-бабочка")

        assertEquals(3, errors.size)
        assertTrue(errors.any { it.startsWith("«носки»: слот «foot»") })
        assertTrue(errors.any { it.startsWith("группа «socks»: limit") })
        assertTrue(errors.any { it.startsWith("папка «галстук-бабочка»") })
    }

    private fun validate(config: String = CONFIG, folders: Set<String> = FOLDERS): List<String> =
        ClothingDatasetValidator.validate(config, folders)

    private companion object {
        val FOLDERS = setOf("трусы", "носки")

        val CONFIG = """
            {
              "levels": ["underwear", "socks"],
              "groups": {
                "socks": { "limit": 1 }
              },
              "sections": [
                { "id": "underwear", "title": "Бельё" },
                { "id": "hosiery", "title": "Носки" }
              ],
              "categories": {
                "трусы": { "slot": "waist", "level": "underwear", "section": "underwear" },
                "носки": { "slot": "feet", "level": "socks", "group": "socks", "section": "hosiery" }
              }
            }
        """.trimIndent()
    }
}
