package ru.vkdev.dressdoll.disco.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vkdev.dressdoll.settings.api.SettingsSection
import ru.vkdev.dressdoll.theme.ui.OptionCard
import ru.vkdev.dressdoll.theme.ui.SectionLabel
import ru.vkdev.dressdoll.theme.ui.Spacing

internal class DiscoSettingsSection(private val settings: DiscoSettingsRepository) : SettingsSection {

    override val order: Int = 100

    override fun LazyListScope.content() {
        item(key = "disco_section") {
            SectionLabel(
                text = stringResource(R.string.disco_settings_section),
                modifier = Modifier.padding(start = Spacing.extraSmall, top = Spacing.large),
            )
        }
        item(key = "disco_styles") {
            val selected by settings.beamStyle.collectAsStateWithLifecycle()
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                BeamStyle.entries.forEach { style ->
                    val (title, hint) = when (style) {
                        BeamStyle.PAPER -> R.string.disco_settings_paper to R.string.disco_settings_paper_hint
                        BeamStyle.SOFT -> R.string.disco_settings_soft to R.string.disco_settings_soft_hint
                    }
                    OptionCard(
                        title = stringResource(title),
                        supportingText = stringResource(hint),
                        selected = selected == style,
                        onClick = { settings.setBeamStyle(style) },
                    )
                }
            }
        }
    }
}
