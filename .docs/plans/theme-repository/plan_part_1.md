# Тема через репозиторий

Создан: 2026-11-17 11:52

Вынесен из плана [doll-outfit](../doll-outfit/plan_part_1.md): с образом куклы не связан, и план образа от него не зависит.

## Зачем

Сейчас `ThemeSelectorImpl` сам работает с `DataStore<Preferences>` и одновременно хранит выбор, кодирует его в строку и применяет логику: значение по умолчанию, сортировку палитр. Это расходится с общим правилом: хранение уходит в репозиторий, логика остаётся в `ThemeSelector`.

Публичный API `:theme:api` (`ThemeSelector`, `ThemeChoice`) и его поведение не меняются: выбор по-прежнему виден в `choice` сразу после `select`. Экран выбора темы и `MainActivity` правок не требуют.

## Принятые решения

Общие с [репозиторием образа](../doll-outfit/plan_part_1.md#принятые-решения):

- Данные читаются и пишутся только через интерфейс репозитория, DataStore и формат скрыты в impl.
- Репозиторий только хранит. Значение по умолчанию и сортировка палитр остаются в селекторе.
- Значение сначала меняется в памяти, на диск пишется в фоне через канал `CONFLATED`. `IOException` при чтении и записи не роняет приложение, значение действует до перезапуска.
- Пока значение не прочитано с диска, репозиторий отдаёт `null`.

## Открытые вопросы

- `ThemeRepository` — `internal` в `:theme:impl`, а не отдельный модуль api/impl. Принято по умолчанию: тему читает и меняет только `ThemeSelector`, отдельный модуль дал бы фичам обходить селектор. Ждёт подтверждения.

## Шаги

### 1. Репозиторий

```kotlin
internal interface ThemeRepository {
    /** Сохранённый выбор; `null` — ещё не прочитан. */
    val saved: StateFlow<Saved?>

    fun save(choice: ThemeChoice)

    /** [choice] — `null`, если ничего не сохранено или значение не распознано. */
    data class Saved(val choice: ThemeChoice?)
}
```

`Saved` отделяет «ещё не прочитано» от «ничего не сохранено». Подставлять значение по умолчанию — дело селектора, а не репозитория.

`ThemeRepositoryImpl(dataStore, scope)` забирает из `ThemeSelectorImpl`:
- чтение `dataStore.data` с `IOException` → «ничего не сохранено»;
- канал `CONFLATED` и запись в фоне с игнорированием `IOException`;
- защиту от затирания: `save` до первого чтения не перекрывается значением с диска;
- `KEY`, `encode`/`decode`.

`save` меняет `saved` синхронно, до записи на диск.

`createThemeDataStore` и файл `theme` остаются как есть: формат на диске не меняется, у пользователей выбор сохранится.

### 2. Селектор

`ThemeSelectorImpl(repository, palettes, scope)`:
- `palettes` сортируются по `order`, как сейчас;
- `choice` — свой `MutableStateFlow`, а не `repository.saved.map { … }.stateIn(…)`: `stateIn` публикует значение только после перехода на поток `scope`, и выбор перестал бы применяться сразу;
- `select(choice)` сначала пишет в `current.value`, затем вызывает `repository.save(choice)`;
- фоновый сборщик переносит `repository.saved` в `current`: `null` → `null`, `Saved(null)` → `DEFAULT_CHOICE`, `Saved(choice)` → `choice`. Сборщик может на мгновение вернуть значение, прочитанное до `select`, но следом всегда придёт `Saved(choice)`: `save` уже поменял `saved`;
- `DEFAULT_CHOICE` остаётся в селекторе.

### 3. Koin

`themeModule`: `single<ThemeRepository> { ThemeRepositoryImpl(get(ThemeDataStore), scope) }`, селектор получает репозиторий вместо DataStore.

### 4. Тесты

Делим `ThemeSelectorImplTest`:

- `ThemeRepositoryImplTest` получает тесты про диск: прочитано с диска новым экземпляром, последний из быстрых выборов, повреждённый файл, сбой записи, сбой чтения, неизвестное значение → `Saved(null)`, симметрия `encode`/`decode`, `save` до первого чтения не затирается, `saved` меняется синхронно в `save`.
- `ThemeSelectorImplTest` работает с поддельным `ThemeRepository` без DataStore: `null` до чтения, затем Корица по умолчанию; `Saved(null)` → Корица; `choice.value` равен выбору сразу после `select`, без прокрутки диспетчера; палитры отсортированы по `order`.
