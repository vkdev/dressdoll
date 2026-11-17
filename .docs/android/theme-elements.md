# Элементы темы Android-приложения

Каталог переиспользуемых элементов темы: токены, иконки и компоненты. Экраны собираются из них, а не из своих `dp`, иконок и цветов. Если нужного элемента нет, его добавляют сюда и в тему, а не делают локальную копию в модуле экрана.

Правила:
- Названия общие, без привязки к конкретному экрану.
- Фигуры, отступы, размеры и линии одни на все палитры. Это константы, а не часть `ThemePalette`.
- Цвета берутся только из ролей M3 активной палитры (`MaterialTheme.colorScheme`). Новых цветов в палитры ради компонента не добавляем.
- Стили текста — роли M3 `Typography` без правки размера (`.copy(fontSize = …)`). Поясняющий текст — `bodySmall` / `onSurfaceVariant`.
- Всё лежит в `:theme:api` (пакет `ru.vkdev.dressdoll.theme.api`), кроме значений `Shapes`: они задаются в `AppShapes` модуля `:theme:impl`. Отдельный модуль компонентов заведём, когда их станет много.
- Размеры, которые есть только у одного экрана и не повторяются (например, полоса цветов 10dp в превью палитры), остаются в модуле экрана: это не общие элементы.

Статус:
- **есть** — реализовано в коде;
- **план** — описано в плане, ещё не реализовано (ссылка на план);
- **кандидат** — нужен существующему экрану, но ещё ни в каком плане. Откуда взят, сказано в разделе «Где найдены кандидаты».

## Токены

### Фигуры

Роли M3 `Shapes`, читаются через `MaterialTheme.shapes`. Значения задаются явно в `AppShapes`, умолчаниями M3 не пользуемся.

| Роль | Радиус | Где используется | Статус |
|---|---|---|---|
| `extraSmall` | 4dp | — | есть |
| `small` | 8dp | — | есть |
| `medium` | 12dp | — | есть |
| `large` | 16dp | `SelectableCard`, `NavigationTile`, `ChoiceTile` | есть |
| `extraLarge` | 28dp | верх шторки (умолчание `BottomSheetDefaults.ExpandedShape`) | есть |

### Отступы — `object Spacing`

| Токен | Значение | Где используется | Статус |
|---|---|---|---|
| `extraSmall` | 4dp | промежуток между кнопкой «Назад» и заголовком в `HeaderBar`, отступ подписи раздела и подвала от края списка, промежуток между строками текста в карточке | есть |
| `small` | 8dp | промежутки между `NavigationTile`, внутренние поля `ChoiceTile`, отступ значка выбора, поле `HeaderBar` слева перед кнопкой «Назад» и справа после кнопок действий, промежуток между текстом и шевроном в `NavigationTile`, промежутки между образцами-чипсами, отступ подвала списка сверху, поля кнопки «Снять всё» справа и над шторкой, поля кнопки дискотеки слева и над шторкой | есть |
| `medium` | 12dp | промежутки между `ChoiceTile` и между карточками в списке, промежутки внутри карточек, отступ подписи раздела сверху | есть |
| `large` | 16dp | поля контейнеров, списков и сеток, поля `NavigationTile` и `SelectableCard`, поля `HeaderBar` справа без кнопок действий и слева без кнопки «Назад» | есть |
| `extraLarge` | 24dp | — | есть |
| `extraExtraLarge` | 32dp | запас под последним элементом прокручиваемого экрана | есть |
| `overhang` | 30dp | запас вокруг содержимого, которое выступает за свои границы: поля вокруг фигурки на экране куклы, куда выходит одежда | есть |

### Размеры — `object Sizes`

| Токен | Значение | Где используется | Статус |
|---|---|---|---|
| `touchTarget` | 48dp | `IconActionButton`, `IconToggleActionButton` и их круглая подложка, любые иконочные кнопки, высота язычка скрытой шторки, минимальная высота `HeaderBar` по умолчанию | есть |
| `topBarHeight` | 64dp | минимальная высота `HeaderBar` в шапке полноэкранного экрана: выбор темы, кукла | есть |
| `icon` | 24dp | иконка в `IconActionButton` и `IconToggleActionButton` | есть |
| `iconSmall` | 18dp | шеврон в `NavigationTile`, галочка в значке выбора | есть |
| `badge` | 24dp | значок выбора в `ChoiceTile` | есть |
| `tileHeight` | 56dp | высота `NavigationTile` | есть |

### Линии — `object Strokes`

| Токен | Значение | Где используется | Статус |
|---|---|---|---|
| `thin` | 1dp | рамка невыбранной `SelectableCard`, обводка кружка «день/ночь» и чипса-образца на экране выбора темы | есть |
| `thick` | 3dp | рамка выбранной `SelectableCard` | есть |

Толщина выбора одна во всём приложении. Взята с экрана выбора темы: там 3dp проверены на всех пяти палитрах.

## Иконки — `object AppIcons`

Свои `ImageVector` в линейном стиле: сетка 24, толщина линии 2, скруглённые концы и углы. `material-icons` не подключаем.

| Иконка | Где используется | Статус |
|---|---|---|
| `ArrowBack` | `BackButton` | есть. Перенесена из `ThemeSettingsScreen` как есть: уголок «<» с `autoMirror = true` |
| `ChevronRight` | `NavigationTile` | есть |
| `Check` | значок выбора в `ChoiceTile` | есть |
| `Settings` | шестерёнка: 8 зубов, ось-кружок. Кнопка настроек в шапке экрана куклы | есть |
| `Hanger` | вешалка: крючок и треугольные плечики. Кнопка «Снять всё» на экране куклы | есть |
| `DiscoBall` | диско-шар: круг с двумя параллелями и тремя меридианами, сверху нитка. Кнопка дискотеки слева внизу на экране куклы | есть |

## Компоненты

| Компонент | Что это | Цвета и типографика | Где используется | Статус |
|---|---|---|---|---|
| `HeadlineText(text, style, …)` | текст заголовка с регистром палитры (`uppercaseHeadlines`) | стиль задаёт вызывающий | экран выбора темы, `HeaderBar` | есть |
| `IconActionButton(icon, contentDescription, onClick, backdropAlpha)` | `IconButton` размером `touchTarget` с иконкой `icon`. `backdropAlpha: () -> Float` — непрозрачность круглой подложки под иконкой, по умолчанию 0; читается при отрисовке | иконка `onSurface`, подложка `surface` | `BackButton`; шапка экрана куклы: настройки; экран куклы: «Снять всё» справа над шторкой, с подложкой во время дискотеки | есть |
| `IconToggleActionButton(icon, contentDescription, checked, onCheckedChange, backdropAlpha)` | иконочная кнопка-переключатель размером `touchTarget` с иконкой `icon`: `toggleable` с ролью `Switch` (M3 `IconToggleButton` озвучивается как флажок), круглая рябь. `backdropAlpha` — как у `IconActionButton` | иконка `onSurface`, во включённом состоянии `primary`; подложка `surface` | экран куклы: дискотека слева над шторкой | есть |
| `BackButton(onClick)` | `IconActionButton` с `AppIcons.ArrowBack`. `contentDescription` «Назад» из ресурсов `:theme:api` | как у `IconActionButton` | `HeaderBar` | есть |
| `HeaderBar(title, onBack, supportingText, titleStyle, minHeight, actions)` | шапка экрана или панели: `BackButton`, если `onBack != null`, иначе отступ до выравнивания с контентом; заголовок через `HeadlineText` с `heading()`; справа поясняющий текст и кнопки `actions` (`IconActionButton`) | заголовок `titleStyle` (по умолчанию `headlineSmall`) / `onSurface`, пояснение `bodySmall` / `onSurfaceVariant`. `minHeight` по умолчанию `touchTarget` | каталог; экран выбора темы и экран куклы (`headlineMedium`, `topBarHeight`) | есть |
| `SelectableCard(selected, onClick, role, color, content)` | карточка выбора. `role: SelectionRole`: `Single` (по умолчанию) — один из многих, `selectable(role = Role.RadioButton)`; `Multiple` — независимый флажок, `toggleable(role = Role.Checkbox)`. Фигура `large`, рамка `Strokes.thick` `primary` у выбранной и `Strokes.thin` `outline` у остальных. Содержимое и индикатор выбора задаёт вызывающий | фон `color` (по умолчанию `surface`) | `ChoiceTile`; экран выбора темы: карточки палитр, `OptionCard` | есть |
| `ChoiceTile(selected, onClick, role, content)` | `SelectableCard` с круглым значком `badge` и `Check` в правом верхнем углу у выбранной; `role` передаётся в `SelectableCard` | фон `surfaceContainerLowest`, значок `primary` / `onPrimary` | каталог: вещи (`SelectionRole.Multiple`) | есть |
| `OptionCard(title, selected, onClick, supportingText, leading)` | вариант настройки «один из многих»: `SelectableCard` с полями `large`, слева необязательный `leading`, заголовок и пояснение, справа `RadioButton` без своего нажатия. Группу `selectableGroup()` задаёт вызывающий | фон `surface`, заголовок `titleMedium` SemiBold, пояснение `bodySmall` / `onSurfaceVariant` | экран настроек: «Как в системе», стиль лучей дискотеки | есть |
| `NavigationTile(text, onClick)` | плашка-переход: текст в одну строку с многоточием, справа `ChevronRight`. Высота `tileHeight`, фигура `large` | фон `surfaceContainerHigh`, текст `titleSmall` / `onSurface` | каталог: категории | есть |
| `SectionLabel(text)` | надзаголовок раздела: капс, разрядка, `heading()` | `labelMedium` полужирный с разрядкой 0.18em / `onSurfaceVariant` | экран выбора темы: «Тема оформления» | есть |

Компоненты выбора не знают про пропорции и группу: квадратность (`aspectRatio`) и `selectableGroup()` задаёт вызывающий. `selectableGroup()` — только для `SelectionRole.Single`: это семантика группы радиокнопок, а флажки независимы. В `SelectableCard` индикатором может быть M3 `RadioButton(onClick = null)`, как на экране выбора темы, или значок, как в `ChoiceTile`.

Рамку `SelectableCard` рисует цветами текущей `MaterialTheme`. Если содержимое карточки показано в другой палитре (превью на экране выбора темы), `SelectableCard` ставят снаружи `MaterialTheme` этой палитры, а внутрь — только содержимое: тогда рамка выбора остаётся цвета активной темы и одинаково заметна на любой карточке.

## Где найдены кандидаты

Сейчас кандидатов нет.
