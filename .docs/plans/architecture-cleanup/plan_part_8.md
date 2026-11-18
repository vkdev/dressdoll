# Элементы темы отдельным модулем

Создан: 2026-11-17 22:30

Часть 8 из 10. Согласие получено 2026-11-18. Изменение затрагивает все экраны и правило CLAUDE.md про элементы темы. Делать после частей 1–3, чтобы не править одни и те же build-файлы дважды.

## Зачем

`:theme:api` совмещает две роли:

- контракт выбора темы — `ThemeSelector` с `select()`, `ThemeChoice`, `AppTheme`, `ThemePalette`, `ThemeIds`, `LocalThemePalette`;
- элементы темы — токены (`Spacing`, `Sizes`, `Strokes`), `AppIcons`, компоненты (`HeaderBar`, `OptionCard`, `SelectableCard`, плитки, кнопки), строка `theme_back`.

Каталог, кукла и дискотека зависят от модуля ради компонентов и поэтому видят `ThemeSelector`: любой экран может сменить тему. Менять её должен только раздел темы в настройках, а читать — `MainActivity` и `:theme:impl`.

## Принятые решения

- Элементы темы переезжают в `:theme:ui`. Название оставляет в силе слова CLAUDE.md и каталога [theme-elements.md](../../android/theme-elements.md) («элементы темы»), меняется только модуль.
- В `:theme:api` остаётся контракт выбора: `AppTheme`, `ThemeSelector`, `ThemeChoice`, `ThemePalette`, `LocalThemePalette`. От него зависят `:app`, `:theme:impl`, палитры и раздел темы в `:settings:impl`.
- `HeadlineText` читает регистр заголовков не из `LocalThemePalette`, а из `LocalUppercaseHeadlines` в `:theme:ui`. Значение задаёт `AppThemeImpl` из палитры, а карточка палитры в настройках — из своей палитры.
- `:theme:ui` зависит только от Compose и Material 3, не от `:theme:api`.

## Открытые вопросы

- ~~Согласны ли на разделение. Название `:theme:ui` — предложение.~~ **Решено (2026-11-18):** разделить, модуль `:theme:ui`.

## Шаги

1. ~~`:theme:ui`: токены, `AppIcons`, компоненты, `HeadlineText`, `LocalUppercaseHeadlines`, строка `theme_back`.~~
2. ~~`AppThemeImpl` и `PaletteCard` задают `LocalUppercaseHeadlines`.~~
3. ~~Экраны и палитры переходят на `:theme:ui`; `:theme:api` остаётся только там, где выбирают или применяют тему.~~
4. ~~Каталог [theme-elements.md](../../android/theme-elements.md) и CLAUDE.md: модуль элементов темы — `:theme:ui`.~~
5. ~~Сборка и тесты, включая контрактные тесты палитр.~~
