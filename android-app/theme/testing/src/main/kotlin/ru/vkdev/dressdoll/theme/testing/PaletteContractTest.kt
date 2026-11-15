package ru.vkdev.dressdoll.theme.testing

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.ResourceFont
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import ru.vkdev.dressdoll.theme.api.ThemePalette

@RunWith(RobolectricTestRunner::class)
abstract class PaletteContractTest(
    private val module: Module,
    private val id: String,
    private val title: String,
    private val uppercaseHeadlines: Boolean = false,
) {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val palette: ThemePalette by lazy {
        val koin = koinApplication { modules(module) }
        try {
            koin.koin.get<ThemePalette>(named(id))
        } finally {
            koin.close()
        }
    }

    @Test
    fun `palette is registered under its id`() {
        assertEquals(id, palette.id)
    }

    @Test
    fun `title comes from resources`() {
        assertEquals(title, context.getString(palette.titleRes))
    }

    @Test
    fun `headline case matches palette`() {
        assertEquals(uppercaseHeadlines, palette.uppercaseHeadlines)
    }

    @OptIn(ExperimentalTextApi::class)
    @Test
    fun `typography fonts are valid font files`() {
        val fonts = palette.typography.resourceFonts()
        assertTrue("typography has no resource fonts", fonts.isNotEmpty())
        fonts.forEach { font ->
            val name = context.resources.getResourceEntryName(font.resId)
            val file = try {
                OpenTypeFont(context.resources.openRawResource(font.resId).use { it.readBytes() })
            } catch (e: IllegalArgumentException) {
                fail("$name: ${e.message}")
                return
            }
            assertTrue("$name has no glyphs", file.hasTable("glyf") || file.hasTable("CFF "))

            val weightSetting = font.variationSettings.settings.firstOrNull { it.axisName == "wght" }
            if (weightSetting != null) {
                val range = file.axes["wght"]
                if (range == null) fail("$name is not a variable font, but sets wght")
                val value = weightSetting.toVariationValue(null)
                assertTrue("$name: wght $value is outside $range", value in range!!)
            } else {
                assertEquals("$name: declared weight differs from the file", file.weightClass, font.weight.weight)
            }
        }
    }
}

private fun Typography.resourceFonts(): Set<ResourceFont> = listOf(
    displayLarge, displayMedium, displaySmall,
    headlineLarge, headlineMedium, headlineSmall,
    titleLarge, titleMedium, titleSmall,
    bodyLarge, bodyMedium, bodySmall,
    labelLarge, labelMedium, labelSmall,
).flatMap { style -> (style.fontFamily as? FontListFontFamily)?.fonts.orEmpty() }
    .filterIsInstance<ResourceFont>()
    .toSet()
