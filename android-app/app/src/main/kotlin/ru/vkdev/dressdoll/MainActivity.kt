package ru.vkdev.dressdoll

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.luminance
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject
import ru.vkdev.dressdoll.navigation.api.AppNavigation
import ru.vkdev.dressdoll.settings.theme.api.ThemeSettingsRoute
import ru.vkdev.dressdoll.theme.api.AppTheme
import ru.vkdev.dressdoll.theme.api.ThemeSelector

class MainActivity : ComponentActivity() {

    private val themeSelector: ThemeSelector by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { themeSelector.choice.value == null }
        super.onCreate(savedInstanceState)
        setContent {
            val theme = koinInject<AppTheme>()
            val navigation = koinInject<AppNavigation>()
            theme.Apply {
                // Палитры светлые или тёмные целиком, поэтому стиль баров зависит
                // только от светлоты фона и обновляется при каждой смене темы.
                val darkBackground = MaterialTheme.colorScheme.background.luminance() < 0.5f
                LaunchedEffect(darkBackground) {
                    val style = if (darkBackground) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                }
                // Временно: выбор темы — стартовый экран. DollRoute по-прежнему зарегистрирован.
                navigation.Host(startRoute = ThemeSettingsRoute)
            }
        }
    }
}
