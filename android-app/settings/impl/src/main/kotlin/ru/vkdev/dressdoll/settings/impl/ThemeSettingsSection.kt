package ru.vkdev.dressdoll.settings.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vkdev.dressdoll.settings.api.SettingsSection
import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.api.ThemeChoice
import ru.vkdev.dressdoll.theme.api.ThemePalette
import ru.vkdev.dressdoll.theme.api.ThemeSelector
import ru.vkdev.dressdoll.theme.ui.HeadlineText
import ru.vkdev.dressdoll.theme.ui.LocalUppercaseHeadlines
import ru.vkdev.dressdoll.theme.ui.OptionCard
import ru.vkdev.dressdoll.theme.ui.SectionLabel
import ru.vkdev.dressdoll.theme.ui.SelectableCard
import ru.vkdev.dressdoll.theme.ui.Spacing
import ru.vkdev.dressdoll.theme.ui.Strokes

internal class ThemeSettingsSection(private val selector: ThemeSelector) : SettingsSection {

    override val order: Int = 0

    override fun LazyListScope.content() {
        val palettes = selector.palettes
        item(key = "theme_section") {
            SectionLabel(
                text = stringResource(R.string.settings_theme_section),
                modifier = Modifier.padding(start = Spacing.extraSmall, top = Spacing.medium),
            )
        }
        item(key = "theme_system") {
            val choice by selector.choice.collectAsStateWithLifecycle()
            SystemItem(
                palettes = palettes,
                selected = choice == ThemeChoice.System,
                onClick = { selector.select(ThemeChoice.System) },
            )
        }
        items(palettes, key = { "theme_palette_${it.id}" }) { palette ->
            val choice by selector.choice.collectAsStateWithLifecycle()
            PaletteCard(
                palette = palette,
                selected = choice == ThemeChoice.Palette(palette.id),
                onClick = { selector.select(ThemeChoice.Palette(palette.id)) },
            )
        }
        item(key = "theme_footer") {
            Text(
                text = stringResource(R.string.settings_theme_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = Spacing.extraSmall,
                    end = Spacing.extraSmall,
                    top = Spacing.small,
                ),
            )
        }
    }
}

@Composable
private fun SystemItem(
    palettes: List<ThemePalette>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val day = palettes.firstOrNull { it.systemDefault == SystemDefault.LIGHT }
    val night = palettes.firstOrNull { it.systemDefault == SystemDefault.DARK }
    val colors = MaterialTheme.colorScheme
    OptionCard(
        title = stringResource(R.string.settings_theme_system),
        selected = selected,
        onClick = onClick,
        supportingText = if (day != null && night != null) {
            stringResource(
                R.string.settings_theme_system_hint,
                stringResource(day.titleRes),
                stringResource(night.titleRes),
            )
        } else {
            null
        },
        leading = {
            Row(
                modifier = Modifier
                    .size(DayNightSize)
                    .clip(CircleShape)
                    .border(Strokes.thin, colors.outline, CircleShape),
            ) {
                listOf(day, night).forEach { palette ->
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(palette?.colorScheme?.background ?: Color.Transparent),
                    )
                }
            }
        },
    )
}

@Composable
private fun PaletteCard(
    palette: ThemePalette,
    selected: Boolean,
    onClick: () -> Unit,
) {
    // Карточка снаружи темы палитры: рамку выбора SelectableCard рисует цветами активной темы,
    // так выбранный пункт одинаково заметен на любой карточке.
    SelectableCard(
        selected = selected,
        onClick = onClick,
        color = palette.colorScheme.background,
        modifier = Modifier.fillMaxWidth(),
    ) {
        MaterialTheme(colorScheme = palette.colorScheme, typography = palette.typography) {
            val colors = MaterialTheme.colorScheme
            // Регистр заголовков и цвет текста по умолчанию заданы для активной темы, поэтому
            // карточка задаёт их заново по своей палитре.
            CompositionLocalProvider(
                LocalUppercaseHeadlines provides palette.uppercaseHeadlines,
                LocalContentColor provides colors.onBackground,
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                    ) {
                        HeadlineText(
                            text = stringResource(palette.titleRes),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.primary,
                            modifier = Modifier.weight(1f),
                        )
                        RadioButton(selected = selected, onClick = null)
                    }
                    Text(
                        text = stringResource(palette.taglineRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                    )
                    ColorStripe()
                    SampleControls()
                }
            }
        }
    }
}

@Composable
private fun ColorStripe() {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ColorStripeHeight)
            .clip(CircleShape)
            .clearAndSetSemantics {},
    ) {
        listOf(
            colors.primary,
            colors.secondary,
            colors.tertiary,
            colors.surfaceVariant,
            colors.onSurface,
        ).forEach { color ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color),
            )
        }
    }
}

// Размеры превью палитры: только у этого экрана, поэтому не элементы темы.
private val DayNightSize = 44.dp
private val ColorStripeHeight = 10.dp
private val SampleButtonMinHeight = 36.dp
private val SampleChipMinHeight = 32.dp

// Образцы, а не настоящие кнопки и чипсы: внутри выбираемой карточки они не должны
// перехватывать нажатия и попадать в озвучку.
@Composable
private fun SampleControls() {
    val colors = MaterialTheme.colorScheme
    val label = MaterialTheme.typography.labelLarge
    // FlowRow и heightIn: при крупном шрифте образцы переносятся, а не сжимаются и обрезаются.
    FlowRow(
        modifier = Modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = SampleButtonMinHeight)
                .background(colors.primary, CircleShape)
                .padding(horizontal = Spacing.large),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.settings_theme_sample_button),
                style = label,
                fontWeight = FontWeight.SemiBold,
                color = colors.onPrimary,
            )
        }
        Box(
            modifier = Modifier
                .heightIn(min = SampleChipMinHeight)
                .border(Strokes.thin, colors.outline, CircleShape)
                .padding(horizontal = Spacing.medium),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.settings_theme_sample_chip_dresses),
                style = label,
                color = colors.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .heightIn(min = SampleChipMinHeight)
                .background(colors.secondaryContainer, CircleShape)
                .padding(horizontal = Spacing.medium),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.settings_theme_sample_chip_shoes),
                style = label,
                color = colors.onSecondaryContainer,
            )
        }
    }
}
