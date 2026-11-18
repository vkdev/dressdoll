package ru.vkdev.dressdoll.theme.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconActionButton(
        icon = AppIcons.ArrowBack,
        contentDescription = stringResource(R.string.theme_back),
        onClick = onClick,
        modifier = modifier,
    )
}
