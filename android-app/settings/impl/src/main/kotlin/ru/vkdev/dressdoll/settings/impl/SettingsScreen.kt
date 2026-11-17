package ru.vkdev.dressdoll.settings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.vkdev.dressdoll.settings.api.SettingsSection
import ru.vkdev.dressdoll.theme.api.HeaderBar
import ru.vkdev.dressdoll.theme.api.Sizes
import ru.vkdev.dressdoll.theme.api.Spacing

@Composable
internal fun SettingsScreen(
    sections: List<SettingsSection>,
    onBack: (() -> Unit)?,
) {
    val insets = WindowInsets.safeDrawing

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            HeaderBar(
                title = stringResource(R.string.settings_title),
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
                sections.forEach { section -> with(section) { content() } }
            }
        }
    }
}
