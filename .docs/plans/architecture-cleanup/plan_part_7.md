# Мёртвый API

Создан: 2026-11-17 22:30

Часть 7 из 10. Делать после [части 5](plan_part_5.md): правится тот же `:clothing:api`.

## Зачем

API, которым не пользуется ни один потребитель, приходится поддерживать, маппить и тестировать, а KDoc обещает поведение, которого нет:

- `catalogSections()`, `CatalogSection`, `CatalogSectionId`, `ClothingCategory.section`. Каталог показывает категории одной сеткой без разделов, а KDoc `clothingCategories()` ссылается на порядок по разделам.
- `ClothingItem.category`.
- `TDClothingItem.parentCategory`.
- `Navigator.replaceAll`: вызывают только тесты, а KDoc `AppNavigation.Host` на него ссылается.

## Принятые решения

- `ClothingItem.category`, `TDClothingItem.parentCategory`, `Navigator.replaceAll` с его тестами и ссылкой в KDoc `AppNavigation` удаляются.
- Разделы в `categories.json` и в генерации конфига остаются в любом случае: по ним сборка упорядочивает категории.

## Открытые вопросы

- Разделы каталога. Варианты:
  - показать в каталоге заголовки разделов над группами категорий (заголовок на всю ширину сетки);
  - удалить из `:clothing:api` и `:clothing:impl` `catalogSections()`, `CatalogSection`, `CatalogSectionId`, `ClothingCategory.section`, а из `TemporaryDataset` — `catalogSections()`.

  По умолчанию — удалить: экрана с разделами в планах нет. Без решения шаг 2 не начинать.

## Шаги

1. Удалить `ClothingItem.category`, `TDClothingItem.parentCategory`, `Navigator.replaceAll` и его тесты, поправить KDoc `AppNavigation.Host`.
2. Разделы — по решению: заголовки в каталоге или удаление из api, маппинга, датасета и тестов маппинга.
3. Сборка и тесты.
