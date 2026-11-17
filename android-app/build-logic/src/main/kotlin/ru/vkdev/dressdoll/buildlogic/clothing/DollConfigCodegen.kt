package ru.vkdev.dressdoll.buildlogic.clothing

import ru.vkdev.dressdoll.buildlogic.clothing.CategoriesConfigCodegen.literal

internal object DollConfigCodegen {

    const val FILE_NAME = "DollConfig.kt"

    fun generate(config: DollConfig): String = buildString {
        appendLine("// Сгенерировано из doll_1.png и scale.json вещей задачей generateDatasetConfig, не редактировать.")
        appendLine("package ${CategoriesConfigCodegen.PACKAGE}")
        appendLine()
        appendLine("internal val dollConfig = TDDollConfig(")
        appendLine("    canvasWidth = ${config.canvas.width},")
        appendLine("    canvasHeight = ${config.canvas.height},")
        with(config.figure) {
            appendLine("    figure = TDCanvasRect(left = $left, top = $top, width = $width, height = $height),")
        }
        appendLine(")")
        appendLine()
        appendLine("internal val itemPlacements: Map<TDItemId, TDCanvasPoint> = mapOf(")
        config.placements.forEach {
            appendLine(
                "    TDItemId(CategoryName(${literal(it.category)}), ${literal(it.folder)}) to TDCanvasPoint(x = ${it.x}, y = ${it.y}),",
            )
        }
        appendLine(")")
    }
}
