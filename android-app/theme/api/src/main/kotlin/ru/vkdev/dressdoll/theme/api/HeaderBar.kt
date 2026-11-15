package ru.vkdev.dressdoll.theme.api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/**
 * Шапка экрана или панели.
 *
 * Без [onBack] кнопки нет, и заголовок выровнен по полям контента ([Spacing.large]).
 */
@Composable
fun HeaderBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    minHeight: Dp = Sizes.touchTarget,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .padding(
                start = if (onBack != null) Spacing.small else Spacing.large,
                end = Spacing.large,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        if (onBack != null) {
            BackButton(onClick = onBack)
        }
        HeadlineText(
            text = title,
            style = titleStyle,
            color = colors.onSurface,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
