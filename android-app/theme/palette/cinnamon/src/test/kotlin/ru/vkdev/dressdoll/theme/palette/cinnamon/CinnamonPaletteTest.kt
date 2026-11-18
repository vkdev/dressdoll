package ru.vkdev.dressdoll.theme.palette.cinnamon

import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.testing.PaletteContractTest

class CinnamonPaletteTest : PaletteContractTest(
    module = cinnamonPaletteModule,
    id = CINNAMON_ID,
    title = "Корица и бисквит",
    tagline = "Замша, терракота и верблюжья шерсть — образы этого сезона.",
    systemDefault = SystemDefault.LIGHT,
)
