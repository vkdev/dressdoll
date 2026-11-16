# Конфиг надевания. Часть 3: данные в репозитории

Создан: 2026-11-16 09:41

Части плана:
1. [Общее и конфиг датасета](plan_part_1.md)
2. [Проверка при сборке](plan_part_2.md)
3. **Данные в репозитории** (этот файл)

## Цель

Слот, уровень, группа и раздел категории доступны через `DollRepository`. Модели `repository:api` не зависят от датасета, чтобы его можно было заменить бэкендом без изменений в фичах.

## Шаги

### 1. Модели `repository:api`

В `ClothingModels.kt`:

- `enum class BodySlot` — `FEET`, `LEGS`, `WAIST`, `TORSO`, `NECK`, `ARMS`, `HEAD`.
- `value class DrawLevelId`, `ReplacementGroupId`, `CatalogSectionId`.
- `data class DrawLevel(id, position: Int) : Comparable<DrawLevel>` — сравнение по `position`. Позиция — только порядок, не z-index: z считает кукла.
- `data class ReplacementGroup(id, limit: Int)`.
- `data class CatalogSection(id, title: String)`.
- `ClothingCategory` получает `slot: BodySlot`, `level: DrawLevel`, `group: ReplacementGroup`, `section: CatalogSectionId`. KDoc ссылается на z-index.md.

### 2. `DollRepository`

- `clothingCategories()` — порядок из конфига, как в датасете.
- Новый `catalogSections(): Flow<List<CatalogSection>>`.

### 3. Маппинг в `repository:impl`

- В `TemporaryDatasetMappers.kt`: `TDSlot → BodySlot`, `TDLevel → DrawLevel`, `TDGroup → ReplacementGroup`, `TDSection → CatalogSection`, расширить `toClothingCategory()`.
- `DollRepositoryImpl.catalogSections()` — через датасет.

### 4. Потребители

- Каталог продолжает показывать плоский список категорий, новые поля не читает. Меняется только порядок плиток: теперь по разделам, а не по алфавиту.
- Проверить, что больше нигде `ClothingCategory(...)` не создаётся напрямую (тесты, превью), и обновить такие места.

### 5. Тесты

- `TemporaryDatasetMappersTest`: каждый `TDSlot` маппится в одноимённый `BodySlot` (по всем значениям, чтобы новый слот без маппинга ловился тестом); уровень, группа и раздел переносятся без потерь.
- `./gradlew test` и `:app:assembleDebug` проходят.
- Запуск приложения: каталог открывается, 34 категории в порядке разделов, вещи открываются как раньше.

### 6. Документация

- В z-index.md отметить, что конфиг и проверка реализованы, а z-index и замена — ещё нет.
