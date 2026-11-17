package ru.vkdev.dressdoll.theme.api

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role

/**
 * Иконочная кнопка шапки: область нажатия [Sizes.touchTarget], иконка [Sizes.icon].
 *
 * [backdropAlpha] — непрозрачность круга `surface` под иконкой, когда кнопка лежит на тёмном или пёстром фоне.
 * Читается при отрисовке: плавное появление круга не вызывает рекомпозиции.
 */
@Composable
fun IconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdropAlpha: () -> Float = { 0f },
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(Sizes.touchTarget)
            .backdrop(MaterialTheme.colorScheme.surface, backdropAlpha),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(Sizes.icon),
        )
    }
}

/** Переключатель в стиле [IconActionButton]. Озвучивается как переключатель, а не флажок, как M3 `IconToggleButton`. */
@Composable
fun IconToggleActionButton(
    icon: ImageVector,
    contentDescription: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    backdropAlpha: () -> Float = { 0f },
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(Sizes.touchTarget)
            .backdrop(colors.surface, backdropAlpha)
            .clip(CircleShape)
            .toggleable(
                value = checked,
                interactionSource = null,
                indication = ripple(),
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (checked) colors.primary else colors.onSurface,
            modifier = Modifier.size(Sizes.icon),
        )
    }
}

private fun Modifier.backdrop(color: Color, alpha: () -> Float): Modifier = drawBehind {
    val value = alpha()
    if (value > 0f) drawCircle(color, alpha = value)
}
