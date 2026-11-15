package ru.vkdev.dressdoll.doll.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.vkdev.dressdoll.doll.api.DollScreen
import ru.vkdev.dressdoll.repository.api.DollRepository

internal class DollScreenImpl(
    private val repository: DollRepository,
) : DollScreen {

    @Composable
    override fun Content() {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "Doll", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
