# Соглашения сборки во всех модулях

Создан: 2026-11-17 21:27

Часть 3 из 5. От частей 1 и 2 не зависит, но после них в модулях меньше конфликтов: лучше делать третьей.

## Зачем

Плагины `androidLibraryCompose` и `featureImpl` есть, но применены через раз:

- `doll:*`, `theme:api`, `theme:impl`, `disco:*`, `outfit:*`, `repository:*`, `temporary-dataset` вручную повторяют `compileSdk`, `minSdk`, `compose = true`, BOM.
- `doll/impl/build.gradle.kts` перечисляет те же зависимости Koin, что добавляет `featureImpl`, а `DollModule.kt` сам ставит `@OptIn(KoinExperimentalAPI::class)`, хотя плагин включает его компилятору.
- Плагина для библиотеки без Compose нет, поэтому модули данных копируют блок `android`.
- Флаг JVM для Robolectric скопирован в `doll:impl` и `temporary-dataset`.
- `doll:api` включает компилятор Compose ради одного `NavKey`.

## Принятые решения

- Новый плагин `dressdoll.android.library`: `android-library`, `compileSdk`, `minSdk`, флаг JVM для Robolectric во всех юнит-тестах. `androidLibraryCompose` применяет его и добавляет Compose, `featureImpl` — как сейчас.
- Модуль экрана с навигацией — `featureImpl`; модуль с Compose без навигации — `androidLibraryCompose`; остальные — `android.library`. Koin в модулях данных подключается явно: плагин ради трёх строк не нужен.
- `doll:api` — `android.library` и сериализация, без Compose. Если навигация потребует компилятор Compose у потребителя `NavKey`, это покажет сборка, тогда вернуть.

## Открытые вопросы

- Нет.

## Шаги

1. `AndroidLibraryPlugin` в `build-logic`, регистрация и псевдоним в `libs.versions.toml`; `AndroidLibraryComposePlugin` на нём.
2. Перевести модули по правилу выше, убрать повторы блока `android`, ручной Koin в `doll:impl`, `@OptIn` в `DollModule`, флаг Robolectric из `doll:impl` и `temporary-dataset`.
3. Сборка `assembleDebug` и все юнит-тесты, включая `build-logic`.
