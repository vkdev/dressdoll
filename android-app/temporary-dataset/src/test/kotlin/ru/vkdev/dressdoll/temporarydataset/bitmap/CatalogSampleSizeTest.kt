package ru.vkdev.dressdoll.temporarydataset.bitmap

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogSampleSizeTest {

    @Test
    fun `long side stays at least the minimum`() {
        assertEquals(2, catalogSampleSize(768, 1024))
        assertEquals(2, catalogSampleSize(1024, 768))
        assertEquals(4, catalogSampleSize(2048, 1536))
        assertEquals(1, catalogSampleSize(1023, 600))
    }

    @Test
    fun `small or unreadable images are not downsampled`() {
        assertEquals(1, catalogSampleSize(CATALOG_MIN_SIDE, CATALOG_MIN_SIDE))
        assertEquals(1, catalogSampleSize(100, 100))
        assertEquals(1, catalogSampleSize(-1, -1))
    }
}
