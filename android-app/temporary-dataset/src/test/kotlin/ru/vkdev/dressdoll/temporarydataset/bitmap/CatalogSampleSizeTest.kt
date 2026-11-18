package ru.vkdev.dressdoll.temporarydataset.bitmap

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogSampleSizeTest {

    @Test
    fun `long side stays at least the minimum`() {
        assertEquals(2, catalogSampleSize(768, 1024, MIN_SIDE))
        assertEquals(2, catalogSampleSize(1024, 768, MIN_SIDE))
        assertEquals(4, catalogSampleSize(2048, 1536, MIN_SIDE))
        assertEquals(1, catalogSampleSize(1023, 600, MIN_SIDE))
    }

    @Test
    fun `small or unreadable images are not downsampled`() {
        assertEquals(1, catalogSampleSize(MIN_SIDE, MIN_SIDE, MIN_SIDE))
        assertEquals(1, catalogSampleSize(100, 100, MIN_SIDE))
        assertEquals(1, catalogSampleSize(-1, -1, MIN_SIDE))
    }

    private companion object {
        const val MIN_SIDE = 512
    }
}
