package ru.vkdev.dressdoll

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject
import ru.vkdev.dressdoll.doll.api.DollRoute
import ru.vkdev.dressdoll.navigation.api.AppNavigation
import ru.vkdev.dressdoll.theme.api.AppTheme
import ru.vkdev.dressdoll.theme.api.LocalThemePalette
import ru.vkdev.dressdoll.theme.api.ThemeSelector

class MainActivity : ComponentActivity() {

    private val themeSelector: ThemeSelector by inject()

    // Заставка ждёт палитру, которая уже попала в композицию, а не значение в StateFlow:
    // иначе между записью выбора и перекомпозицией успевает отрисоваться кадр схемы M3.
    private var themeApplied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !themeApplied }
        super.onCreate(savedInstanceState)
        // До первой раскладки, чтобы контент сразу получил системные отступы.
        applySystemBars(darkBackground = false)
        setContent {
            val theme = koinInject<AppTheme>()
            val navigation = koinInject<AppNavigation>()
            theme.Apply {
                // Без палитр тема так и останется стандартной M3, ждать нечего.
                val applied = LocalThemePalette.current != null || themeSelector.palettes.isEmpty()
                SideEffect { themeApplied = applied }

                // Палитры светлые или тёмные целиком, поэтому стиль баров зависит
                // только от светлоты фона и обновляется при каждой смене темы.
                // DisposableEffect срабатывает до отрисовки кадра, в отличие от LaunchedEffect.
                val darkBackground = MaterialTheme.colorScheme.background.luminance() < 0.5f
                DisposableEffect(darkBackground) {
                    applySystemBars(darkBackground)
                    onDispose {}
                }
                navigation.Host(startRoute = DollRoute)
            }
        }
    }

    private fun applySystemBars(darkBackground: Boolean) {
        val style = if (darkBackground) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
