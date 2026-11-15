package ru.vkdev.dressdoll.theme.api

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase

/** `null`, если палитр нет и действует стандартная схема M3. */
val LocalThemePalette = staticCompositionLocalOf<ThemePalette?> { null }

// Регистр заголовков нельзя задать через TextStyle: в нём нет аналога text-transform.
@Composable
fun HeadlineText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val uppercase = LocalThemePalette.current?.uppercaseHeadlines == true
    Text(
        text = if (uppercase) text.toUpperCase(LocaleList.current) else text,
        style = style,
        modifier = modifier,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}
