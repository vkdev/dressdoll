package ru.vkdev.dressdoll.buildlogic.clothing

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

/**
 * Объекты — `Map<String, Any?>` в порядке файла, массивы — `List<Any?>`.
 * Из повторённых ключей остаётся первое значение, повтор попадает в [duplicateKeys].
 */
internal class JsonTree(val root: Any?, val duplicateKeys: List<DuplicateKey>)

/** [path] — ключи от корня до объекта, в котором повторился [key]. */
internal data class DuplicateKey(val path: List<String>, val key: String)

/**
 * Свой обход потока Jackson, а не JsonSlurper или дерево Jackson: те молча оставляют
 * одно значение повторённого ключа, а повтор категории в конфиге — это ошибка.
 *
 * @throws com.fasterxml.jackson.core.JsonProcessingException если текст — не JSON.
 */
internal fun readJsonTree(text: String): JsonTree {
    val duplicates = mutableListOf<DuplicateKey>()
    JsonFactory().createParser(text).use { parser ->
        if (parser.nextToken() == null) throw JsonParseException(parser, "пустой документ")
        val root = parser.readValue(emptyList(), duplicates)
        if (parser.nextToken() != null) throw JsonParseException(parser, "после корневого значения есть данные")
        return JsonTree(root, duplicates)
    }
}

private fun JsonParser.readValue(path: List<String>, duplicates: MutableList<DuplicateKey>): Any? =
    when (currentToken()) {
        JsonToken.START_OBJECT -> {
            val map = LinkedHashMap<String, Any?>()
            while (nextToken() == JsonToken.FIELD_NAME) {
                val key = currentName()
                nextToken()
                val value = readValue(path + key, duplicates)
                if (key in map) duplicates += DuplicateKey(path, key) else map[key] = value
            }
            map
        }
        JsonToken.START_ARRAY -> buildList {
            while (nextToken() != JsonToken.END_ARRAY) add(readValue(path, duplicates))
        }
        JsonToken.VALUE_STRING -> text
        JsonToken.VALUE_NUMBER_INT -> numberValue
        JsonToken.VALUE_NUMBER_FLOAT -> decimalValue
        JsonToken.VALUE_TRUE -> true
        JsonToken.VALUE_FALSE -> false
        JsonToken.VALUE_NULL -> null
        else -> throw JsonParseException(this, "неожиданный токен ${currentToken()}")
    }
