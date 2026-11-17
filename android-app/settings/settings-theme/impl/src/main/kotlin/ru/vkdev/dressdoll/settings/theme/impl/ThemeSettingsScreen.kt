package ru.vkdev.dressdoll.settings.theme.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.vkdev.dressdoll.disco.api.BeamStyle
import ru.vkdev.dressdoll.disco.api.DiscoSettingsRepository
import ru.vkdev.dressdoll.theme.api.HeaderBar
import ru.vkdev.dressdoll.theme.api.HeadlineText
import ru.vkdev.dressdoll.theme.api.LocalThemePalette
import ru.vkdev.dressdoll.theme.api.OptionCard
import ru.vkdev.dressdoll.theme.api.SectionLabel
import ru.vkdev.dressdoll.theme.api.SelectableCard
import ru.vkdev.dressdoll.theme.api.Sizes
import ru.vkdev.dressdoll.theme.api.Spacing
import ru.vkdev.dressdoll.theme.api.Strokes
import ru.vkdev.dressdoll.theme.api.ThemeChoice
import ru.vkdev.dressdoll.theme.api.ThemeIds
import ru.vkdev.dressdoll.theme.api.ThemePalette
import ru.vkdev.dressdoll.theme.api.ThemeSelector

// Фразы карточек живут здесь, а не в ThemePalette: это текст экрана выбора, а не свойство темы.
// Id остальных палитр объявлены в их модулях, от которых экран не зависит, поэтому строками.
@StringRes
private fun taglineRes(paletteId: String): Int? = when (paletteId) {
    ThemeIds.CINNAMON -> R.string.settings_theme_tagline_cinnamon
    ThemeIds.DISCO -> R.string.settings_theme_tagline_disco
    "avocado" -> R.string.settings_theme_tagline_avocado
    "powder" -> R.string.settings_theme_tagline_powder
    "denim" -> R.string.settings_theme_tagline_denim
    else -> null
}

@Composable
internal fun ThemeSettingsScreen(
    selector: ThemeSelector,
    discoSettings: DiscoSettingsRepository,
    onBack: (() -> Unit)?,
) {
    val choice by selector.choice.collectAsState()
    val beamStyle by discoSettings.beamStyle.collectAsState()
    val palettes = selector.palettes
    val colors = MaterialTheme.colorScheme
    val insets = WindowInsets.safeDrawing

    Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
        Column {
            HeaderBar(
                title = stringResource(R.string.settings_theme_title),
                onBack = onBack,
                titleStyle = MaterialTheme.typography.headlineMedium,
                minHeight = Sizes.topBarHeight,
                modifier = Modifier.windowInsetsPadding(
                    insets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                ),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(insets.only(WindowInsetsSides.Horizontal))
                    .selectableGroup(),
                contentPadding = PaddingValues(
                    start = Spacing.large,
                    end = Spacing.large,
                    bottom = Spacing.extraExtraLarge + insets.only(WindowInsetsSides.Bottom)
                        .asPaddingValues()
                        .calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                item(key = "section") {
                    SectionLabel(
                        text = stringResource(R.string.settings_theme_section),
                        modifier = Modifier.padding(start = Spacing.extraSmall, top = Spacing.medium),
                    )
                }
                item(key = "system") {
                    SystemItem(
                        palettes = palettes,
                        selected = choice == ThemeChoice.System,
                        onClick = { selector.select(ThemeChoice.System) },
                    )
                }
                items(palettes, key = { it.id }) { palette ->
                    PaletteCard(
                        palette = palette,
                        selected = choice == ThemeChoice.Palette(palette.id),
                        onClick = { selector.select(ThemeChoice.Palette(palette.id)) },
                    )
                }
                item(key = "footer") {
                    Text(
                        text = stringResource(R.string.settings_theme_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = Spacing.extraSmall,
                            end = Spacing.extraSmall,
                            top = Spacing.small,
                        ),
                    )
                }
                item(key = "disco_section") {
                    SectionLabel(
                        text = stringResource(R.string.settings_disco_section),
                        modifier = Modifier.padding(start = Spacing.extraSmall, top = Spacing.large),
                    )
                }
                item(key = "disco_styles") {
                    BeamStyleOptions(selected = beamStyle, onSelect = discoSettings::setBeamStyle)
                }
            }
        }
    }
}

@Composable
private fun BeamStyleOptions(selected: BeamStyle, onSelect: (BeamStyle) -> Unit) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        BeamStyle.entries.forEach { style ->
            val (title, hint) = when (style) {
                BeamStyle.PAPER -> R.string.settings_disco_paper to R.string.settings_disco_paper_hint
                BeamStyle.SOFT -> R.string.settings_disco_soft to R.string.settings_disco_soft_hint
            }
            OptionCard(
                title = stringResource(title),
                supportingText = stringResource(hint),
                selected = selected == style,
                onClick = { onSelect(style) },
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
    val day = palettes.firstOrNull { it.id == ThemeIds.CINNAMON }
    val night = palettes.firstOrNull { it.id == ThemeIds.DISCO }
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
            // HeadlineText берёт регистр заголовков из LocalThemePalette. Цвет текста по умолчанию
            // Surface карточки вычислил по активной теме, поэтому задаём его заново.
            CompositionLocalProvider(
                LocalThemePalette provides palette,
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
                    taglineRes(palette.id)?.let { res ->
                        Text(
                            text = stringResource(res),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                        )
                    }
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
                .heightIn(min = 36.dp)
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
                .heightIn(min = 32.dp)
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
                .heightIn(min = 32.dp)
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
