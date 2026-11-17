package ru.vkdev.dressdoll.outfit.impl

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal object OutfitSerializer : Serializer<OutfitFile> {

    // Поля, добавленные новыми версиями приложения, не ломают чтение старой.
    // encodeDefaults: без него version со значением по умолчанию не попадает в файл,
    // и будущая миграция не отличит старый формат от нового.
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val defaultValue: OutfitFile = OutfitFile()

    override suspend fun readFrom(input: InputStream): OutfitFile = try {
        json.decodeFromString(OutfitFile.serializer(), input.readBytes().decodeToString())
    } catch (error: SerializationException) {
        throw CorruptionException("Не удалось разобрать файл образа", error)
    } catch (error: IllegalArgumentException) {
        throw CorruptionException("Не удалось разобрать файл образа", error)
    }

    override suspend fun writeTo(t: OutfitFile, output: OutputStream) {
        output.write(json.encodeToString(OutfitFile.serializer(), t).encodeToByteArray())
    }
}

internal fun createOutfitDataStore(scope: CoroutineScope, file: () -> File): DataStore<OutfitFile> =
    DataStoreFactory.create(
        serializer = OutfitSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { OutfitFile() },
        scope = scope,
        produceFile = file,
    )
