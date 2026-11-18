# Каталог: держатель состояния и правила ошибок репозитория

Создан: 2026-11-17 22:30

Часть 6 из 10. Делать после [части 5](plan_part_5.md): здесь меняются те же интерфейс и потребители, а имена ниже — уже после переименования (`ClothingRepository`, `:clothing:api`).

## Зачем

- У каталога нет держателя состояния. `ItemsPane` сам подписывается на `clothingItems()` в `LaunchedEffect` и складывает результат в `mutableStateMapOf` из композиции. Каждая плитка сама вызывает `catalogImage()` и ловит `IOException`. Репозиторий передаётся вниз до плитки.
- Поэтому у `:catalog:impl` нет ни одного теста: проверять можно только UI. У куклы та же работа вынесена в `DollStateHolder`.
- Что делать при ошибке, вызывающий угадывает сам:
  - `catalogImage()` возвращает `null`, если файла нет, и бросает `IOException`, если файл не читается;
  - `dollImages()` бросает `FileNotFoundException`, `IOException` или `IllegalStateException`;
  - `figure()` бросает `FileNotFoundException`.

  В интерфейсе это не описано. Кукла ловит ошибку в держателе, каталог — в UI, а ошибку `figure()` не ловит никто: она молча останавливает поток `scene`, и кукла не появляется.

## Принятые решения

- Правила ошибок `ClothingRepository` записываются в KDoc интерфейса:
  - картинки, которой может не быть (`catalogImage`, задняя плоскость), нет — `null`;
  - файл есть, но не читается — `IOException`, и потребитель решает, что показать;
  - нарушение инварианта датасета, которое проверяет сборка (нет позиции вещи, нет фигурки), — `IllegalStateException`. Его не ловят: это ошибка сборки, а не данных.
- `figure()` при отсутствии фигурки бросает `IllegalStateException`, а не `FileNotFoundException`: сборка проверяет `doll_1.png`.
- `CatalogStateHolder` — `internal` в `:catalog:impl`, `single` в Koin, как `DollStateHolder`:
  - `categories: StateFlow<List<ClothingCategory>?>`, `null` до загрузки;
  - `items(category): StateFlow<List<ClothingItem>?>` — кеш по категории, повторно открытая категория сразу показывает прошлый список;
  - `suspend fun image(item): ImageBitmap?` — `IOException` превращается в `null` здесь, а не в плитке.
- Открытая категория и прокрутка остаются UI-состоянием в `CatalogImpl` (`rememberSaveable`).

## Открытые вопросы

- Нет.

## Шаги

1. ~~KDoc правил ошибок в `ClothingRepository`; `figure()` и `dollImages()` в `TemporaryDataset` бросают `IllegalStateException` при нарушении инварианта.~~
2. ~~`CatalogStateHolder` и тесты на фейковом репозитории: загрузка категорий, кеш вещей, `IOException` картинки → `null`.~~
3. ~~`CatalogImpl` и панели берут данные из держателя, репозиторий из UI уходит.~~
4. ~~`DollStateHolder.loadImages` ловит только `IOException`, как сейчас; проверить, что остальное соответствует правилам.~~
5. ~~Сборка и тесты.~~
