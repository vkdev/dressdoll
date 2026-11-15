package ru.vkdev.dressdoll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.compose.koinInject
import ru.vkdev.dressdoll.doll.api.DollRoute
import ru.vkdev.dressdoll.navigation.api.AppNavigation
import ru.vkdev.dressdoll.theme.api.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme = koinInject<AppTheme>()
            val navigation = koinInject<AppNavigation>()
            theme.Apply {
                navigation.Host(startRoute = DollRoute)
            }
        }
    }
}
