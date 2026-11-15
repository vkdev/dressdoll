# Темы 70-х и навигация. Часть 2: темы

Создан: 2026-11-15 21:01

Общие решения, модули и зависимости описаны в [части 1](plan_part_1.md). Это отдельный коммит.

**Выполнено 2026-11-15.** Сборка, unit-тесты (селектор, выбор палитры, 5 Robolectric-тестов палитр) и запуск на эмуляторе со всеми пятью палитрами прошли. Не закоммичено.

Отличия от исходного плана:
- В `ThemePalette` добавлено поле `order`: Koin `getAll` не гарантирует порядок, а в выборе темы палитры идут в порядке холста. `ThemeSelectorImpl` сортирует по нему.
- Где у шрифта переменная насыщенность, взят один variable-файл (`variableFontFamily(resId, weights…)` в `:theme:common`). `Font(…, variationSettings)` в Compose экспериментальный, opt-in стоит только на этой функции.
- Montserrat Alternates (текстовый шрифт Диско-ночи) лежит в модуле `disco`: в `:theme:common` только общие Manrope и Rubik.
- Id остальных палитр объявлены в их модулях: `AVOCADO_ID`, `POWDER_ID`, `DENIM_ID`.
- Роли `ColorScheme`, которых нет в таблице (контейнеры, on-цвета, ступени `surfaceContainer`), выводит `paletteColorScheme(base, dark)` в `:theme:common` с проверкой контраста. В модулях палитр хранятся только 9 базовых цветов. Цвета ошибок стандартные M3.
- Межбуквенный интервал задан только у заголовков «Джинса и охры» (1sp), остальные палитры берут значения M3.
- DataStore — файл `theme`, в Koin с квалификатором `named("theme")`.
- Значение в DataStore: `system` или `palette:<id>`; нечитаемое значение даёт выбор по умолчанию.
- `select()` применяет выбор сразу, в памяти, а на диск пишет через очередь с одним потребителем (порядок нажатий сохраняется). Ошибка записи не роняет приложение: выбор действует до перезапуска. Повреждённый файл заменяется пустым (`ReplaceFileCorruptionHandler`).
- Общие тесты палитр вынесены в модуль `:theme:testing` (`PaletteContractTest`): каждый модуль палитры только наследует его. Проверка шрифтов разбирает сам файл: заголовок sfnt, диапазон оси `wght` у variable-шрифтов, `usWeightClass` у статических.
- `values-night/themes.xml` (тёмное окно до старта Compose) не трогали: это часть 3.

### ~~6. `:theme:api`~~
- `ThemePalette`: `id: String`, `@StringRes titleRes: Int`, `colorScheme: ColorScheme`, `typography: Typography`, `uppercaseHeadlines: Boolean`.
- `ThemeChoice`: `System` или `Palette(id)`.
- `ThemeSelector`: `choice: StateFlow<ThemeChoice?>` (`null` означает «ещё не загружено»), `palettes: List<ThemePalette>`, `select(choice)`.
- `LocalThemePalette` и `HeadlineText(text, style)`. `HeadlineText` переводит текст в верхний регистр, если палитра этого требует: в `TextStyle` нет аналога `text-transform`.
- Константы `ThemeIds.CINNAMON` и `ThemeIds.DISCO`. Это осознанное исключение: правило «Как в системе» опирается на два конкретных id.
- Зависимость на material3 (нужны типы `ColorScheme` и `Typography`).

### ~~7. `:theme:common`~~
- `res/font/`: Manrope и Rubik.
- `assets/licenses/OFL-<Font>.txt` для всех 8 семейств: Yeseva One, Manrope, Playfair Display, Montserrat Alternates, Prata, Rubik, Cormorant, Oswald.
- `buildTypography(display: FontSpec, body: FontSpec)`, где `FontSpec` включает шрифт, насыщенность, начертание и интервал.

### ~~8. Пять палитр~~
- Convention-плагин `dressdoll.theme.palette` в `build-logic`: `dressdoll.android.library.compose` + `:theme:api` + `:theme:common` + material3 + koin. Его ID добавить в `[plugins]` каталога. Все пять модулей палитр подключают этот плагин.
- В каждом модуле:
  - `res/font/` с заголовочным шрифтом;
  - `strings.xml` с названием;
  - `<Name>Colors.kt` с полной `ColorScheme` (у Диско-ночи `darkColorScheme`);
  - `<Name>Typography.kt`;
  - Koin-модуль `single<ThemePalette>(named(id)) { … }`. Квалификатор обязателен, иначе определения перекроют друг друга.
- Yeseva One и Prata используются только с `FontWeight.Normal`, чтобы не было искусственного полужирного.
- У «Джинса и охры» `uppercaseHeadlines = true`.
- Цвета взяты с холста:

| Палитра | primary / onPrimary | secondary | tertiary | background | surface | surfaceVariant | onSurface | outline | Шрифты |
|---|---|---|---|---|---|---|---|---|---|
| Корица и бисквит | #B5532E / #FFFFFF | #D9A23A | #6F7140 | #F3E6D0 | #FBF4E8 | #E8D3B4 | #3A2414 | #A88C70 | Yeseva One + Manrope |
| Диско-ночь (dark) | #E0559A / #24122E | #D4A84B | #B9A6D6 | #24122E | #321C3F | #43284F | #F6E9F0 | #7D6589 | Playfair Display It 900 + Montserrat Alternates |
| Авокадо и горчица | #5F6E2F / #FFFFFF | #E0A526 | #C8602A | #F5EEDC | #FFFAEE | #E4DDB8 | #3B2A1E | #A89A74 | Prata + Rubik |
| Пудра и бордо | #6E1F2E / #FFF6F2 | #D9A5A0 | #8E9A7A | #F7E8E2 | #FFF6F2 | #EBCFC8 | #2D2426 | #B0948F | Cormorant It 700 + Manrope |
| Джинс и охра | #2F4A6D / #FFFFFF | #C98A2B | #A4452C | #EFE4D2 | #FAF4EA | #D9CBB2 | #1F2733 | #9A9080 | Oswald 600 (uppercase) + Rubik |

### ~~9. `:theme:impl`~~
- DataStore Preferences в Koin как `single`, ключ `theme_choice`.
- `ThemeSelectorImpl`:
  - палитры берёт из `getAll<ThemePalette>()`;
  - `choice == null`, пока не прочитано первое значение;
  - если ничего не сохранено, выбор равен `Palette(CINNAMON)`.
- Выбор активной палитры:
  - `System` → Диско при `isSystemInDarkTheme()`, иначе Корица;
  - нужной палитры нет → первая доступная;
  - палитр нет совсем → стандартная схема M3.
- `AppThemeImpl.Apply` задаёт `MaterialTheme` и `LocalThemePalette`. Пока `choice == null`, используется схема по умолчанию: экран в это время закрыт заставкой.
- Удалить `Color.kt` и `Type.kt`, `Shape.kt` оставить.

### ~~10. Подключение в `:app`~~
- Зависимости на 5 палитр и их Koin-модули в `startKoin`. Это должно войти в тот же коммит, иначе приложение без палитр не запустится.

### ~~11. Проверка~~
- Unit-тесты `ThemeSelectorImpl` (временный файл DataStore, `kotlinx-coroutines-test`):
  - сначала `null`, затем значение по умолчанию;
  - выбор сохраняется;
  - правило «Как в системе»;
  - откат при неизвестном id и при пустом списке палитр.
- Robolectric-тест в каждом модуле палитры: шрифты резолвятся, название берётся из ресурсов.
