package ru.vkdev.dressdoll.buildlogic.clothing

import org.junit.Assert.assertEquals
import org.junit.Test

class DollConfigCodegenTest {

    @Test
    fun `генерирует размер холста, фигурку и позиции вещей`() {
        val config = DollConfig(
            canvas = ImageSize(768, 1376),
            figure = CanvasRect(left = 221, top = 66, width = 329, height = 1226),
            placements = listOf(
                ItemPlacement("блузка", "01", x = 215, y = 266),
                ItemPlacement("ботинки", "02", x = -4, y = 1178),
            ),
        )

        assertEquals(
            """
            // Сгенерировано из doll_1.png и scale.json вещей задачей generateDatasetConfig, не редактировать.
            package ru.vkdev.dressdoll.temporarydataset

            internal val dollConfig = TDDollConfig(
                canvasWidth = 768,
                canvasHeight = 1376,
                figure = TDCanvasRect(left = 221, top = 66, width = 329, height = 1226),
            )

            internal val itemPlacements: Map<TDItemId, TDCanvasPoint> = mapOf(
                TDItemId(CategoryName("блузка"), "01") to TDCanvasPoint(x = 215, y = 266),
                TDItemId(CategoryName("ботинки"), "02") to TDCanvasPoint(x = -4, y = 1178),
            )

            """.trimIndent(),
            DollConfigCodegen.generate(config),
        )
    }
}
