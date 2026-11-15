package ru.vkdev.dressdoll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.compose.koinInject
import ru.vkdev.dressdoll.doll.api.DollScreen
import ru.vkdev.dressdoll.theme.api.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme = koinInject<AppTheme>()
            val dollScreen = koinInject<DollScreen>()
            theme.Apply {
                dollScreen.Content()
            }
        }
    }
}
