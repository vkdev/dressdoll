package ru.vkdev.dressdoll.theme.palette.disco

import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.testing.PaletteContractTest

class DiscoPaletteTest : PaletteContractTest(
    module = discoPaletteModule,
    id = DISCO_ID,
    title = "Диско-ночь",
    tagline = "Люрекс, сливовый бархат и золото — до самого утра.",
    systemDefault = SystemDefault.DARK,
)
