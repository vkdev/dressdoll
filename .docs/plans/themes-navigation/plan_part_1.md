# Темы 70-х и навигация. Часть 1: общее и навигация

Создан: 2026-11-15 21:01

Части плана:
1. **Общее и навигация** (этот файл)
2. [Темы](plan_part_2.md)
3. [Заставка и системные бары](plan_part_3.md)
4. [Экран выбора темы](plan_part_4.md)

Макеты лежат на холсте «Палитры 70-х»: обзор палитр, экраны в каждой теме, экран «Настройки · выбор темы».

## Общее для всех частей

### Принятые решения

- Пять палитр: Корица и бисквит, Диско-ночь, Авокадо и горчица, Пудра и бордо, Джинс и охра.
- Темы отличаются только цветами и типографикой: шрифты, насыщенность, межбуквенные интервалы, регистр заголовков. Shapes и отступы общие.
- Тёмных вариантов нет. Есть шестой пункт «Как в системе»: днём Корица, ночью Диско-ночь. Явно выбранная палитра действует всегда, независимо от системы.
- Тема по умолчанию: Корица и бисквит.
- Шрифты хранятся в приложении (`res/font`). Общие текстовые шрифты (Manrope, Rubik) лежат в `:theme:common`, заголовочные в модуле своей палитры. Размер шрифтов пока не оптимизируем.
- Лицензии OFL хранятся в `assets/licenses/` модуля `:theme:common`.
- Превью в выборе темы: живые карточки-образцы без надзаголовка. На карточке название палитры её заголовочным шрифтом, фраза, полоса из 5 цветов, кнопка и чипсы. Тема применяется сразу при нажатии, кнопки «Применить» нет.
- Навигация: Navigation 3 (`androidx.navigation3`, стабильная 1.2.1). Экраны регистрируются через Koin DSL `navigation<Route> {}` (`koin-compose-navigation3`, Koin 4.2.2).
- Временно главным (стартовым) экраном становится выбор темы.

### Модули

```
:navigation:api                       AppNavigation, Navigator, LocalNavigator; api(navigation3-runtime)
:navigation:impl                      AppNavigationImpl (NavDisplay + бэкстек), navigationModule

:theme:api                            ThemePalette, ThemeChoice, ThemeSelector, AppTheme, LocalThemePalette, HeadlineText
:theme:common                         Manrope, Rubik, лицензии OFL, buildTypography(...)
:theme:impl                           ThemeSelectorImpl (DataStore), AppThemeImpl, themeModule
:theme:testing                        PaletteContractTest — общие тесты модулей палитр (testImplementation)
:theme:palette:cinnamon               ┐
:theme:palette:disco                  │ зависят от :theme:api и :theme:common;
:theme:palette:avocado                │ ColorScheme, Typography, заголовочный шрифт,
:theme:palette:powder                 │ название в strings.xml, Koin-модуль
:theme:palette:denim                  ┘

settings/
  :settings:settings-theme:api        ThemeSettingsRoute
  :settings:settings-theme:impl       экран выбора темы, themeSettingsModule
```

### Что добавить в version catalog

- ~~`androidx.navigation3:navigation3-runtime` и `navigation3-ui` версии 1.2.1.~~
- ~~`io.insert-koin:koin-compose-navigation3` (версия из Koin BOM).~~
- ~~Плагин `org.jetbrains.kotlin.plugin.serialization` и `org.jetbrains.kotlinx:kotlinx-serialization-core`.~~
- `androidx.datastore:datastore-preferences` (часть 2).
- ~~`androidx.core:core-splashscreen` (часть 3).~~
- ~~Для `build-logic`: артефакты AGP и Compose Compiler Gradle Plugin.~~ Добавлен ещё `kotlin-gradle-plugin-api` (`compileOnly`): нужен для opt-in в `dressdoll.feature.impl`. ID convention-плагинов тоже лежат в каталоге (`libs.plugins.dressdoll.*`).

### Открытые вопросы

- `settings-theme` делим на api/impl, как остальные модули. Принято по умолчанию, ждёт подтверждения.
- API Koin для Navigation 3 помечено `@KoinExperimentalAPI`. При обновлении Koin нужно проверять `navigation<T> {}` и `koinEntryProvider`.

---

## ~~Часть 1: навигация (отдельный коммит)~~

Выполнено 2026-11-15. Сборка, unit-тесты и запуск на эмуляторе (поворот, убийство процесса) прошли. Не закоммичено.

### ~~1. `build-logic`~~
- В `settings.gradle.kts` добавить `pluginManagement { includeBuild("build-logic") }`.
- В `build-logic/settings.gradle.kts` подключить каталог: `versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }`.
- AGP и Compose Compiler Gradle Plugin подключить в `build-logic` как `compileOnly`.
- Плагины:
  - `dressdoll.android.library.compose`: library + compose;
  - `dressdoll.feature.impl`: library + compose + koin + `koin-compose-navigation3` + opt-in `org.koin.core.annotation.KoinExperimentalAPI` на уровне модуля;
  - ~~`dressdoll.theme.palette`~~ перенесён в часть 2: он зависит от `:theme:common`, которого пока нет.
- Существующие модули на эти плагины не переводим.

### ~~2. `:navigation:api`~~
```kotlin
interface AppNavigation {
    @Composable
    fun Host(startRoute: NavKey)
}

interface Navigator {
    val canGoBack: Boolean
    fun navigate(route: NavKey)
    fun back(): Boolean
    fun replaceAll(route: NavKey)
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator is not provided") }
```
- `navigation3-runtime` отдаётся наружу через `api(...)`. Koin в api-модуль не тянем.

### ~~3. `:navigation:impl`~~
```kotlin
internal class AppNavigationImpl : AppNavigation {
    @Composable
    override fun Host(startRoute: NavKey) {
        val backStack = rememberNavBackStack(startRoute)
        val navigator = remember(backStack) { NavigatorImpl(backStack) }
        CompositionLocalProvider(LocalNavigator provides navigator) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.back() },
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                entryProvider = koinEntryProvider<NavKey>(),
            )
        }
    }
}

val navigationModule = module {
    single<AppNavigation> { AppNavigationImpl() }
}
```
- Бэкстек живёт в composition (`rememberNavBackStack`), а не в Koin-скоупе. Так он переживает и поворот экрана, и смерть процесса. Из Koin берётся только `AppNavigation`, а `Navigator` раздаётся экранам через `LocalNavigator`.
- Все маршруты помечены `@Serializable` и реализуют `NavKey`.
- ViewModel-декоратор (`lifecycle-viewmodel-navigation3`) подключим, когда в проекте появятся ViewModel.

### ~~4. Кукла через навигацию (чтобы коммит можно было проверить)~~
- `:doll:api`: `@Serializable data object DollRoute : NavKey`.
- `dollModule`: `navigation<DollRoute> { DollScreen(repository = koinInject()) }`. Интерфейс `DollScreen` из `:doll:api` убран, экран стал `internal` composable-функцией в `:doll:impl`. Так же регистрируются и остальные экраны: зависимости берутся через `koinInject()`, он запоминает значение между перекомпозициями.
- `:app`: добавить `navigationModule` в `startKoin`. В `MainActivity`: `theme.Apply { koinInject<AppNavigation>().Host(startRoute = DollRoute) }`.

### ~~5. Проверка~~
- ~~Unit-тест `NavigatorImpl` на обычном `MutableList`: `navigate`, `back`, `canGoBack`, `replaceAll`.~~ Те же тесты гоняются и на настоящем `NavBackStack`.
- ~~Приложение запускается и выглядит как раньше.~~
