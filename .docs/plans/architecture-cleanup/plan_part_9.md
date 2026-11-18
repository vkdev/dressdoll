# Скоуп приложения и диспетчеры через Koin

Создан: 2026-11-17 22:30

Часть 9 из 10. Делать после [части 4](plan_part_4.md): `PersistedValue` принимает скоуп, и здесь он начинает приходить из одного места.

## Зачем

Скоупы уровня приложения создаются по месту, каждый своим выражением `CoroutineScope(SupervisorJob() + Dispatchers.X)`:

- в Koin-модулях: DataStore и селектор темы, DataStore и репозиторий образа, DataStore и репозиторий дискотеки, `DollStateHolder`;
- внутри `BitmapRepository` — свой скоуп из переданного диспетчера.

Конструкторы классов уже принимают скоуп, поэтому юнит-тестам это не мешает. Мешает другое: время жизни и диспетчеры приложения разбросаны по восьми местам — семь в Koin-модулях и одно в `BitmapRepository`, который скоуп снаружи не получает вовсе.

## Принятые решения

- Модуль `:core:coroutines` (`android.library`, корутины, Koin): квалификатор `ApplicationScope` и класс `AppDispatchers(io, default)`, Koin-модуль `coroutinesModule` с одним `SupervisorJob`-скоупом на всё приложение.
- Классы получают скоуп и диспетчер параметрами, как сейчас; меняется только Koin-модуль: `get(ApplicationScope) + get<AppDispatchers>().io` вместо нового скоупа. В коде это помощник `applicationScope { io }`.
- `CatalogStateHolder` из части 6 тоже на общем скоупе.
- `BitmapRepository` и `TemporaryDataset` получают скоуп параметром.

## Открытые вопросы

- Нет.

## Шаги

1. ~~`:core:coroutines`, подключение в `:app`.~~
2. ~~Перевести Koin-модули `theme`, `outfit`, `disco`, `doll`, `clothing` на общий скоуп и диспетчеры; скоуп в `BitmapRepository` и `TemporaryDataset` — параметром.~~
3. ~~Сборка и тесты.~~
