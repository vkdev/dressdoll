# Дискотека — отдельная фича

Создан: 2026-11-17 21:27

Часть 2 из 5. Опирается на [часть 1](plan_part_1.md): раздел настроек дискотеки к этому моменту уже в `:disco:impl`.

## Зачем

Сейчас в `:disco` только хранилище стиля лучей, а сама дискотека — `DiscoDrawing.kt`, `DiscoLights.kt`, `DiscoState.kt` и их тесты — лежит в `:doll:impl`. Знание о стиле разбросано: `BeamStyle` в `:disco:api`, его смысл для отрисовки (`DiscoLook.airAlpha`, `figureAlpha`) в `:doll:impl`.

Дискотека к тому же знает, как кукла вписывается в экран: `spotTarget` вызывает `fitFigure` и берёт `CanvasRect` из `:repository:api`. А прицел передаётся через скрытый канал: `DiscoStage.aimInArea` подставляет `SideEffect` в `DollScreen`. Жизненный цикл режима (`Saver`, `ON_STOP`, значки статус-бара) занимает треть `DollScreen`.

## Принятые решения

- `:disco:api` отдаёт один вход, по образцу `Catalog`:

  ```kotlin
  interface Disco {
      @Composable
      fun rememberStage(): DiscoStage
  }

  @Stable
  interface DiscoStage {
      var isOn: Boolean
      val dim: Float
      val screen: Modifier
      val header: Modifier

      @Composable
      fun litFigure(figure: () -> Rect): Modifier
  }
  ```

- При реализации уточнено: `dim` — `Float` на состоянии, а не лямбда; `screen` и `header` — готовые модификаторы, созданные один раз, а не расширения: их ставят через `then`, и рекомпозиция экрана их не пересоздаёт. Оформление (`DiscoLook`) и прицел они читают при отрисовке. `litFigure` — `@Composable`: свежую лямбду прицела он передаёт через `rememberUpdatedState`, без `SideEffect` на стороне куклы.
- Кукла отдаёт дискотеке только прямоугольник фигурки, а не свою геометрию. Правило «пока кукла не помещается, целиться в середину области над шторкой» остаётся у куклы: она отдаёт пустой прямоугольник в этой точке. `fitFigure` и `CanvasRect` из дискотеки уходят.
- `discoFigureArea` сливается с `discoLitFigure`: `DollCanvas` занимает всю область, поэтому координаты слоя и области совпадают.
- Внутрь `:disco:impl` уходят: `DiscoState`, `DiscoLights`, отрисовка, `DiscoLook`, `Saver` флага, сброс по `ON_STOP`, значки статус-бара. `DiscoSettingsRepository` и `BeamStyle` становятся `internal`: после части 1 снаружи их никто не читает. `:disco:api` больше не нужен `kotlinx.coroutines`.
- Кнопка шара остаётся в `DollScreen` вместе со строкой `doll_disco`: где кнопка стоит и как едет за шторкой, решает экран куклы. Она связана со `stage.isOn` и `stage.dim`.
- Имя модуля `:disco` сохраняется, в `:spotlights` не переименовываем: палитра «Диско» живёт в `theme.palette.disco`, пакеты не пересекаются.
- Длительности и параметры лучей остаются константами `:disco:impl`, не токенами темы.

## Открытые вопросы

- Нет.

## Шаги

1. ~~Перенести `DiscoLights.kt`, `DiscoState.kt`, `DiscoDrawing.kt` и тесты `DiscoLightsTest`, `DiscoStateTest` в `:disco:impl`, пакет `ru.vkdev.dressdoll.disco.impl`. `:disco:api` и `:disco:impl` — на `androidLibraryCompose`; в `:disco:impl` — Compose UI, Material 3, lifecycle-runtime-compose, activity-compose.~~
2. ~~Прицел от прямоугольника: `spotTarget(aim, figure: Rect)`, тест на доли прямоугольника вместо `fitFigure`. В `:doll:impl` — функция прямоугольника фигурки в координатах области с запасным пустым прямоугольником, тест рядом с `FigureFitTest`.~~
3. ~~`Disco` и `DiscoStage` в `:disco:api`, реализация в `:disco:impl`; `DiscoStage` из `DiscoDrawing.kt` переименовать во внутренний `BeamLayout`, чтобы не путать с публичным. `DiscoLook` собирается внутри из `MaterialTheme.colorScheme` и стиля из репозитория. `BeamStyle`, `DiscoSettingsRepository` → `internal`.~~
4. ~~`DollScreen`: `disco.rememberStage()` вместо `DiscoState`, `DiscoStage`, `Saver`, `LifecycleEventEffect` и `DisposableEffect` статус-бара; `DollModule` внедряет `Disco`.~~
5. ~~Документация: раздел «Дискотека» из [doll-canvas.md](../../doll/doll-canvas.md) — в `.docs/disco/disco-mode.md`, в `doll-canvas.md` остаётся ссылка и то, что кукла отдаёт прямоугольник фигурки. В [плане disco-mode](../disco-mode/plan_part_1.md): «Часть 1 из 2», раздел «Как устроено» отсылает сюда; непроверенные пункты остаются.~~
6. ~~Сборка и тесты.~~ Проверка на эмуляторе не делалась: включение, поворот, уход в настройки и смена стиля после переноса не проверены.
