package ru.vkdev.dressdoll.theme.api

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier.size(Sizes.touchTarget)) {
        Icon(
            imageVector = AppIcons.ArrowBack,
            contentDescription = stringResource(R.string.theme_back),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(Sizes.icon),
        )
    }
}
