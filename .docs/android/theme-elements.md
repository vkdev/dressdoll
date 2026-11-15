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
| `extraSmall` | 4dp | промежуток между кнопкой «Назад» и заголовком в `HeaderBar` | есть |
| `small` | 8dp | промежутки между `NavigationTile`, внутренние поля `ChoiceTile`, отступ значка выбора, поле `HeaderBar` слева перед кнопкой «Назад», промежуток между текстом и шевроном в `NavigationTile` | есть |
| `medium` | 12dp | промежутки между `ChoiceTile` и между карточками в списке | есть |
| `large` | 16dp | поля контейнеров, списков и сеток, поля `NavigationTile` и `SelectableCard`, поля `HeaderBar` справа и слева без кнопки «Назад» | есть |
| `extraLarge` | 24dp | — | есть |
| `extraExtraLarge` | 32dp | запас под последним элементом прокручиваемого экрана | план ([catalog-sheet, часть 3](../plans/catalog-sheet/plan_part_3.md)) |

### Размеры — `object Sizes`

| Токен | Значение | Где используется | Статус |
|---|---|---|---|
| `touchTarget` | 48dp | `BackButton` и любые иконочные кнопки, высота язычка скрытой шторки, минимальная высота `HeaderBar` по умолчанию | есть |
| `topBarHeight` | 64dp | минимальная высота `HeaderBar` в шапке полноэкранного экрана | план ([catalog-sheet, часть 3](../plans/catalog-sheet/plan_part_3.md)) |
| `icon` | 24dp | иконка в `BackButton` | есть |
| `iconSmall` | 18dp | шеврон в `NavigationTile`, галочка в значке выбора | есть |
| `badge` | 24dp | значок выбора в `ChoiceTile` | есть |
| `tileHeight` | 56dp | высота `NavigationTile` | есть |

### Линии — `object Strokes`

| Токен | Значение | Где используется | Статус |
|---|---|---|---|
| `thin` | 1dp | рамка невыбранной `SelectableCard` | есть |
| `thick` | 3dp | рамка выбранной `SelectableCard` | есть |

Толщина выбора одна во всём приложении. Взята с экрана выбора темы: там 3dp проверены на всех пяти палитрах.

## Иконки — `object AppIcons`

Свои `ImageVector` в линейном стиле: сетка 24, толщина линии 2, скруглённые концы и углы. `material-icons` не подключаем.

| Иконка | Где используется | Статус |
|---|---|---|
| `ArrowBack` | `BackButton` | есть. Перенесена из `ThemeSettingsScreen` как есть: уголок «<» с `autoMirror = true` |
| `ChevronRight` | `NavigationTile` | есть |
| `Check` | значок выбора в `ChoiceTile` | есть |

## Компоненты

| Компонент | Что это | Цвета и типографика | Где используется | Статус |
|---|---|---|---|---|
| `HeadlineText(text, style, …)` | текст заголовка с регистром палитры (`uppercaseHeadlines`) | стиль задаёт вызывающий | экран выбора темы, `HeaderBar` | есть |
| `BackButton(onClick)` | `IconButton` размером `touchTarget` с `AppIcons.ArrowBack`. `contentDescription` «Назад» из ресурсов `:theme:api` | иконка `onSurface` | `HeaderBar` | есть |
| `HeaderBar(title, onBack, supportingText, titleStyle, minHeight)` | шапка экрана или панели: `BackButton`, если `onBack != null`, иначе отступ до выравнивания с контентом; заголовок через `HeadlineText` с `heading()`; справа поясняющий текст | заголовок `titleStyle` (по умолчанию `headlineSmall`) / `onSurface`, пояснение `bodySmall` / `onSurfaceVariant`. `minHeight` по умолчанию `touchTarget` | каталог; экран выбора темы (`headlineMedium`, `topBarHeight`) — с части 3 | есть |
| `SelectableCard(selected, onClick, color, content)` | карточка выбора одного из многих: `selectable(role = Role.RadioButton)`, фигура `large`, рамка `Strokes.thick` `primary` у выбранной и `Strokes.thin` `outline` у остальных. Содержимое и индикатор выбора задаёт вызывающий | фон `color` (по умолчанию `surface`) | `ChoiceTile`; экран выбора темы: «Как в системе» и карточки палитр — с части 3 | есть |
| `ChoiceTile(selected, onClick, content)` | `SelectableCard` с круглым значком `badge` и `Check` в правом верхнем углу у выбранной | фон `surfaceContainerLowest`, значок `primary` / `onPrimary` | каталог: вещи | есть |
| `NavigationTile(text, onClick)` | плашка-переход: текст в одну строку с многоточием, справа `ChevronRight`. Высота `tileHeight`, фигура `large` | фон `surfaceContainerHigh`, текст `titleSmall` / `onSurface` | каталог: категории | есть |
| `SectionLabel(text)` | надзаголовок раздела: капс, разрядка, `heading()` | `labelMedium` полужирный с разрядкой 0.18em / `onSurfaceVariant` | экран выбора темы: «Тема оформления» | план ([catalog-sheet, часть 3](../plans/catalog-sheet/plan_part_3.md)) |

Компоненты выбора не знают про пропорции и группу: квадратность (`aspectRatio`) и `selectableGroup()` задаёт вызывающий. В `SelectableCard` индикатором может быть M3 `RadioButton(onClick = null)`, как на экране выбора темы, или значок, как в `ChoiceTile`.

## Где найдены кандидаты

### `ThemeSettingsScreen` (`:settings:settings-theme:impl`)

Что на экране становится элементом темы и что при переводе изменится внешне. Перевод — [часть 3 плана catalog-sheet](../plans/catalog-sheet/plan_part_3.md); после него раздел удаляется.

| На экране сейчас | Элемент темы | Что меняется |
|---|---|---|
| `TopBar`: `IconButton` + `AppIcons.ArrowBack`, `headlineMedium`, высота от 64dp, без стрелки заголовок с 24dp от края | `HeaderBar(titleStyle = headlineMedium, minHeight = Sizes.topBarHeight)` | без стрелки заголовок с 16dp (`Spacing.large`), по полям списка |
| ~~`BackArrow`~~ | `AppIcons.ArrowBack` | уже переведено |
| строка `settings_theme_back` | строка «Назад» в `:theme:api` | строка удаляется из модуля экрана |
| `SectionLabel` | `SectionLabel` | ничего |
| `SystemItem`: `Surface` 20dp + рамка + `selectable` | `SelectableCard(color = surface)` | радиус 20 → 16dp |
| `PaletteCard`: `Surface` 22dp + рамка + `selectable` | `SelectableCard(color = background палитры)` | радиус 22 → 16dp |
| рамки 3dp `primary` / 1dp `outline` | `Strokes.thick` / `Strokes.thin` | ничего |
| поля списка 16dp, промежутки 12dp, запас снизу 32dp | `Spacing.large`, `Spacing.medium`, `Spacing.extraExtraLarge` | ничего |
| поля `SystemItem` 16 × 14dp, промежуток 14dp | `Spacing.large`, `Spacing.medium` | 14 → 16 и 12dp |
| `bodySmall.copy(fontSize = 13.sp)` в подсказке и подвале | `bodySmall` | 13 → 12sp |

Остаются в модуле экрана — это превью палитры, а не общие элементы: кружок «день/ночь» 44dp, полоса цветов `ColorStripe`, образцы кнопки и чипсов `SampleControls`. Их промежутки переходят на `Spacing`.
