package ru.vkdev.dressdoll.theme.testing

import java.nio.ByteBuffer

internal class OpenTypeFont(bytes: ByteArray) {

    private val buffer = ByteBuffer.wrap(bytes)
    private val tables: Map<String, Int>

    init {
        require(bytes.size >= HEADER_SIZE) { "file is too small to be a font" }
        val version = buffer.getInt(0)
        require(version == TRUETYPE || version == OPENTYPE_CFF) {
            "not a TrueType/OpenType font (sfnt version 0x${version.toUInt().toString(16)})"
        }
        val count = buffer.getShort(4).toUShort().toInt()
        tables = (0 until count).associate { index ->
            val record = HEADER_SIZE + index * TABLE_RECORD_SIZE
            tag(record) to buffer.getInt(record + 8)
        }
    }

    fun hasTable(tag: String): Boolean = tag in tables

    val axes: Map<String, ClosedFloatingPointRange<Float>> by lazy {
        val fvar = tables["fvar"] ?: return@lazy emptyMap()
        val axesOffset = fvar + buffer.getShort(fvar + 4).toUShort().toInt()
        val axisCount = buffer.getShort(fvar + 8).toUShort().toInt()
        val axisSize = buffer.getShort(fvar + 10).toUShort().toInt()
        (0 until axisCount).associate { index ->
            val axis = axesOffset + index * axisSize
            tag(axis) to fixed(axis + 4)..fixed(axis + 12)
        }
    }

    val weightClass: Int?
        get() = tables["OS/2"]?.let { buffer.getShort(it + 4).toUShort().toInt() }

    private fun tag(offset: Int) = String(ByteArray(4) { buffer.get(offset + it) }, Charsets.US_ASCII)

    private fun fixed(offset: Int) = buffer.getInt(offset) / 65536f

    private companion object {
        const val TRUETYPE = 0x00010000
        const val OPENTYPE_CFF = 0x4F54544F // "OTTO"
        const val HEADER_SIZE = 12
        const val TABLE_RECORD_SIZE = 16
    }
}
