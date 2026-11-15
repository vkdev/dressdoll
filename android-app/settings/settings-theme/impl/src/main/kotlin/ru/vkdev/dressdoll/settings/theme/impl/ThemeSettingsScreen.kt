package ru.vkdev.dressdoll.settings.theme.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.vkdev.dressdoll.theme.api.HeadlineText
import ru.vkdev.dressdoll.theme.api.LocalThemePalette
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
    onBack: (() -> Unit)?,
) {
    val choice by selector.choice.collectAsState()
    val palettes = selector.palettes
    val colors = MaterialTheme.colorScheme
    // Рамка выбора рисуется цветами активной темы, а не палитры карточки: так выбранный
    // пункт одинаково заметен на любой карточке.
    val selectedBorder = BorderStroke(3.dp, colors.primary)
    val idleBorder = BorderStroke(1.dp, colors.outline)
    val insets = WindowInsets.safeDrawing

    Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
        Column {
            TopBar(
                onBack = onBack,
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
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 32.dp + insets.only(WindowInsetsSides.Bottom).asPaddingValues()
                        .calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "section") {
                    SectionLabel(stringResource(R.string.settings_theme_section))
                }
                item(key = "system") {
                    SystemItem(
                        palettes = palettes,
                        selected = choice == ThemeChoice.System,
                        border = if (choice == ThemeChoice.System) selectedBorder else idleBorder,
                        onClick = { selector.select(ThemeChoice.System) },
                    )
                }
                items(palettes, key = { it.id }) { palette ->
                    val selected = choice == ThemeChoice.Palette(palette.id)
                    PaletteCard(
                        palette = palette,
                        selected = selected,
                        border = if (selected) selectedBorder else idleBorder,
                        onClick = { selector.select(ThemeChoice.Palette(palette.id)) },
                    )
                }
                item(key = "footer") {
                    Text(
                        text = stringResource(R.string.settings_theme_footer),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = BackArrow,
                    contentDescription = stringResource(R.string.settings_theme_back),
                )
            }
        } else {
            Box(Modifier.width(12.dp))
        }
        HeadlineText(
            text = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.toUpperCase(LocaleList.current),
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.18.em,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = 4.dp, top = 12.dp)
            .semantics { heading() },
    )
}

@Composable
private fun SystemItem(
    palettes: List<ThemePalette>,
    selected: Boolean,
    border: BorderStroke,
    onClick: () -> Unit,
) {
    val day = palettes.firstOrNull { it.id == ThemeIds.CINNAMON }
    val night = palettes.firstOrNull { it.id == ThemeIds.DISCO }
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    Surface(
        shape = shape,
        color = colors.surface,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.dp, colors.outline, CircleShape),
            ) {
                listOf(day, night).forEach { palette ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(palette?.colorScheme?.background ?: Color.Transparent),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_theme_system),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (day != null && night != null) {
                    Text(
                        text = stringResource(
                            R.string.settings_theme_system_hint,
                            stringResource(day.titleRes),
                            stringResource(night.titleRes),
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@Composable
private fun PaletteCard(
    palette: ThemePalette,
    selected: Boolean,
    border: BorderStroke,
    onClick: () -> Unit,
) {
    MaterialTheme(colorScheme = palette.colorScheme, typography = palette.typography) {
        // HeadlineText берёт регистр заголовков из LocalThemePalette.
        CompositionLocalProvider(LocalThemePalette provides palette) {
            val colors = MaterialTheme.colorScheme
            val shape = RoundedCornerShape(22.dp)
            Surface(
                shape = shape,
                color = colors.background,
                border = border,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
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
                    .height(10.dp)
                    .background(color),
            )
        }
    }
}

// Образцы, а не настоящие кнопки и чипсы: внутри выбираемой карточки они не должны
// перехватывать нажатия и попадать в озвучку.
@Composable
private fun SampleControls() {
    val colors = MaterialTheme.colorScheme
    val label = MaterialTheme.typography.labelLarge
    // FlowRow и heightIn: при крупном шрифте образцы переносятся, а не сжимаются и обрезаются.
    FlowRow(
        modifier = Modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 36.dp)
                .background(colors.primary, CircleShape)
                .padding(horizontal = 16.dp),
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
                .border(1.dp, colors.outline, CircleShape)
                .padding(horizontal = 14.dp),
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
                .padding(horizontal = 14.dp),
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

// В проекте нет material-icons ради одной стрелки.
private val BackArrow: ImageVector = ImageVector.Builder(
    name = "BackArrow",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    autoMirror = true,
).path(
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 2f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
) {
    moveTo(15f, 5f)
    lineTo(8f, 12f)
    lineTo(15f, 19f)
}.build()
