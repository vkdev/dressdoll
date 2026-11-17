# Репозиторий одежды и мелочи

Создан: 2026-11-17 21:27

Часть 5 из 5. Независима от остальных.

## Зачем

- `:repository` — название слоя, а не предметной области. Внутри данные одежды и куклы: категории, разделы каталога, картинки, фигурка.
- `DollRepository.onTrimMemory` и `onLowMemory` — забота кеша, протекшая в доменный интерфейс. Из-за неё `DressDollApplication` знает про репозиторий.
- `MainActivity.kt:51`: комментарий «входа на экран темы пока нет» устарел — на экране куклы есть шестерёнка.

## Принятые решения

- `:repository:api` / `:repository:impl` → `:clothing:api` / `:clothing:impl`, пакеты `ru.vkdev.dressdoll.clothing.api` / `.impl`. `DollRepository` → `ClothingRepository`, `DollRepositoryImpl` → `ClothingRepositoryImpl`, `repositoryModule` → `clothingModule`. Модели (`DollFigure`, `DollImages` и остальные) сохраняют имена.
- Память освобождает сам `:repository:impl`: при создании регистрирует `ComponentCallbacks2` на контексте приложения (`createdAtStart`). `onTrimMemory` и `onLowMemory` уходят из `DollRepository` и из `DressDollApplication`.

## Открытые вопросы

- Нет.

## Шаги

1. Переименовать модуль, пакеты и интерфейс; обновить зависимости `catalog`, `outfit`, `doll`, `app` и документацию в `.docs/doll/`.
2. Регистрация `ComponentCallbacks2` в `:repository:impl`, методы памяти убрать из интерфейса и `DressDollApplication`; тест, что вызов доходит до `TemporaryDataset.trimMemory`.
3. Удалить устаревший комментарий в `MainActivity`.
4. Сборка и тесты.
