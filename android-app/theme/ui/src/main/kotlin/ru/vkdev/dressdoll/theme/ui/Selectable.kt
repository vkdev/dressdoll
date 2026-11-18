package ru.vkdev.dressdoll.theme.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role

/** Как озвучивается выбор в [SelectableCard]. */
enum class SelectionRole {
    /** Один из многих: `selectable`, роль радиокнопки. Группу `selectableGroup()` задаёт вызывающий. */
    Single,

    /** Независимый флажок: `toggleable`, роль флажка. */
    Multiple,
}

/**
 * Карточка выбора. Индикатор выбора рисует [content], пропорции задаёт вызывающий.
 * [role] — один из многих или независимый флажок.
 */
@Composable
fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: SelectionRole = SelectionRole.Single,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large
    Surface(
        shape = shape,
        color = color,
        border = if (selected) {
            BorderStroke(Strokes.thick, colors.primary)
        } else {
            BorderStroke(Strokes.thin, colors.outline)
        },
        modifier = modifier
            .clip(shape)
            .then(
                when (role) {
                    SelectionRole.Single -> Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                    SelectionRole.Multiple -> Modifier.toggleable(value = selected, role = Role.Checkbox) { onClick() }
                },
            ),
        content = content,
    )
}

/** [SelectableCard] со значком-галочкой в углу у выбранной плитки. */
@Composable
fun ChoiceTile(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: SelectionRole = SelectionRole.Single,
    content: @Composable () -> Unit,
) {
    SelectableCard(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        role = role,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Box(Modifier.padding(Spacing.small)) {
            content()
            if (selected) {
                SelectedBadge(Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

// Без contentDescription: выбор уже озвучен через selectable или toggleable.
@Composable
private fun SelectedBadge(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(Sizes.badge)
            .background(colors.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Check,
            contentDescription = null,
            tint = colors.onPrimary,
            modifier = Modifier.size(Sizes.iconSmall),
        )
    }
}
