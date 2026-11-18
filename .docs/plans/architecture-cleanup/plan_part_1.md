# Общий экран настроек с разделами от фич

Создан: 2026-11-17 21:27

Часть 1 из 10 плана по итогам архитектурного ревью. Готовит почву для [части 2](plan_part_2.md): после неё настройки не знают ни про одну фичу.

## Зачем

Модуль `:settings:settings-theme` по названию — настройки темы, но в нём же раздел «Дискотека» со своими строками и зависимостью от `:disco:api`. Каждая новая настройка фичи тянула бы в него ещё одну зависимость, а экран рос бы ветками `when`.

## Принятые решения

- Модуль переименовывается в `:settings:api` / `:settings:impl`, пакеты `ru.vkdev.dressdoll.settings.api` / `.impl`. `ThemeSettingsRoute` → `SettingsRoute`, `ThemeSettingsScreen` → `SettingsScreen`, `themeSettingsModule` → `settingsModule`. Строки `settings_theme_title` → `settings_title`.
- Раздел экрана — интерфейс в `:settings:api`, его реализации регистрируют сами фичи в Koin, экран собирает их через `getAll()`. Так же уже подключаются палитры.

  ```kotlin
  interface SettingsSection {
      /** Меньше — выше на экране. */
      val order: Int

      /** Ключи элементов уникальны в пределах всего экрана: начинаются с имени фичи. */
      fun LazyListScope.content()
  }
  ```

- Раздел темы остаётся в `:settings:impl` и регистрируется так же, как раздел фичи: тема — не фича, а оформление всего приложения, и `ThemeSelector` уже в `:theme:api`. `order = 0`.
- Раздел дискотеки переезжает в `:disco:impl` (`order = 100`) вместе со своими строками: `settings_disco_*` → `disco_settings_*`. `:settings:impl` перестаёт зависеть от `:disco:api`.
- Сохранённый бэкстек с `ThemeSettingsRoute` после обновления приложения не восстановится. Это не важно: приложение ещё не выпускалось.

## Открытые вопросы

- Нет.

## Шаги

1. ~~Переименовать модули, пакеты, маршрут, экран, Koin-модуль и строки. Обновить `settings.gradle.kts`, `:app`, `:doll:impl` (`DollModule`, зависимость).~~
2. ~~`SettingsSection` в `:settings:api`; экран строит `LazyColumn` из отсортированных разделов. Шапка экрана и отступы остаются у экрана.~~
3. ~~Раздел темы — `ThemeSettingsSection` в `:settings:impl`: текущие «Как в системе», карточки палитр и подпись.~~
4. ~~Раздел дискотеки — `DiscoSettingsSection` в `:disco:impl` со строками; `:disco:impl` зависит от `:settings:api` и `:theme:api` (`SectionLabel`, `OptionCard`).~~
5. ~~Каталог [theme-elements.md](../../android/theme-elements.md): у `ArrowBack` заменить `ThemeSettingsScreen` на `SettingsScreen`, у `OptionCard` указать, что раздел дискотеки отдаёт `:disco:impl`.~~
6. ~~Сборка и тесты.~~ ~~Проверка на эмуляторе~~: экран настроек с двумя разделами проверен 2026-11-18 (Pixel 10a).
