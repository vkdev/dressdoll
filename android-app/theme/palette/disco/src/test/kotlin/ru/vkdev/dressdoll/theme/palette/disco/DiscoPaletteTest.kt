package ru.vkdev.dressdoll.theme.palette.disco

import ru.vkdev.dressdoll.theme.api.ThemeIds
import ru.vkdev.dressdoll.theme.testing.PaletteContractTest

class DiscoPaletteTest : PaletteContractTest(
    module = discoPaletteModule,
    id = ThemeIds.DISCO,
    title = "Диско-ночь",
    tagline = "Люрекс, сливовый бархат и золото — до самого утра.",
)
