# Общий помощник для значения в DataStore

Создан: 2026-11-17 21:27

Часть 4 из 5. Связана с планом [theme-repository](../theme-repository/plan_part_1.md): тот ещё не начат и должен взять помощник отсюда, а не копировать код в четвёртый раз.

## Зачем

`ThemeSelectorImpl`, `DiscoSettingsRepositoryImpl` и `OutfitRepositoryImpl` почти построчно повторяют одно и то же: значение в `MutableStateFlow`, выбор пользователя в `@Volatile`-поле, чтобы чтение с диска его не затёрло, фоновая запись через канал `CONFLATED`, перехват `IOException` при чтении и записи. Исправление в одной копии (например, порядок `update`) придётся повторять в трёх.

## Принятые решения

- Модуль `:core:datastore` (`android.library`, `androidx.datastore.core`, корутины), без Koin:

  ```kotlin
  class PersistedValue<S, T>(
      dataStore: DataStore<S>,
      scope: CoroutineScope,
      initial: T,
      /** Что считать прочитанным, если чтение упало с `IOException`. */
      onReadError: S,
      read: (S) -> T,
      write: (S, T) -> S,
  ) {
      val value: StateFlow<T>
      fun set(value: T)
  }
  ```

- `initial` у образа и темы — `null` (ещё не прочитано), у стиля лучей — `PAPER`. Это решает репозиторий, помощник только хранит.
- Тесты гонок и сбоев (выбор до чтения не затирается, сбой чтения и записи не роняет) переезжают в `:core:datastore`. В тестах репозиториев остаются кодирование и значения по умолчанию.

## Открытые вопросы

- Нет.

## Шаги

1. `:core:datastore` с `PersistedValue` и тестами на файле во временной папке.
2. `OutfitRepositoryImpl` и `DiscoSettingsRepositoryImpl` на помощнике, лишние тесты убрать.
3. В плане [theme-repository](../theme-repository/plan_part_1.md) заменить описание фоновой записи ссылкой на `PersistedValue`.
4. Сборка и тесты.
