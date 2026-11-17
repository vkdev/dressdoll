# Образ куклы. Часть 1: репозиторий образа

Создан: 2026-11-17 11:52

Части плана:
1. **Репозиторий образа** (этот файл)
2. [Отрисовка куклы](plan_part_2.md)

Перевод темы на репозиторий вынесен в отдельный план [theme-repository](../theme-repository/plan_part_1.md): он следует тем же решениям, но от него этот план не зависит.

## Общее для всех частей

### Принятые решения

- Все данные, и контент, и состояние пользователя, читаются и пишутся только через интерфейс репозитория. DataStore, assets и формат файла скрыты в impl.
- Репозиторий только хранит. Правила (замена по группе, значение по умолчанию, проверка по датасету) живут у потребителя.
- Репозитории устроены как `ThemeSelectorImpl` сейчас: значение сначала меняется в памяти, на диск пишется в фоне через канал `CONFLATED`. `IOException` при чтении и записи не роняет приложение, значение действует до перезапуска.
- Пока значение не прочитано с диска, репозиторий отдаёт `null`.

### Модули

```
:outfit:api    OutfitRepository, WornItem; api(:repository:api) ради ClothingItemId, api(coroutines)
:outfit:impl   OutfitRepositoryImpl (DataStore + JSON), outfitModule
```

### Открытые вопросы

- Название модуля `outfit`. Принято по умолчанию, ждёт подтверждения.
- Формат файла образа: JSON через kotlinx.serialization в типизированном DataStore. Принято по умолчанию: состояние будет расти (режим «заправлен», крепление броши, сохранённые образы), строку с разделителями пришлось бы переделывать.
- Общий помощник для хранимого значения. `OutfitRepositoryImpl` и `ThemeRepositoryImpl` из плана [theme-repository](../theme-repository/plan_part_1.md) повторяют одну схему: чтение с `catch` для `IOException`, запись через канал `CONFLATED`, защита от затирания до первого чтения. Её можно вынести в класс вроде `PersistedValue<T>`, но для него нужен общий модуль, которого пока нет. Принято по умолчанию: пока пишем схему в каждом репозитории, выносим, когда появится третий.

## Что не входит в план

Правила надевания, проверка сохранённого образа по датасету, переход каталога на `worn`/`onToggle` и отрисовка слоёв — в [части 2](plan_part_2.md).

## Шаги

### ~~1. Version catalog~~

- ~~`androidx.datastore:datastore` (типизированный DataStore, версия `datastore`).~~
- ~~`org.jetbrains.kotlinx:kotlinx-serialization-json` (версия `kotlinxSerialization`). Плагин `kotlin-serialization` и `kotlinx-serialization-core` уже есть.~~

### ~~2. Модули~~

- ~~`include(":outfit:api")`, `include(":outfit:impl")` в `settings.gradle.kts`.~~
- ~~`:outfit:api` — Android library без Compose.~~
- ~~`:outfit:impl` — Android library, плагин `kotlin-serialization`, зависимости: datastore, serialization-json, coroutines, Koin. Тесты: junit, coroutines-test.~~

### ~~3. API~~

```kotlin
interface OutfitRepository {
    /** Надетые вещи в порядке надевания; `null`, пока образ не прочитан с диска. */
    val worn: StateFlow<List<WornItem>?>

    fun save(worn: List<WornItem>)
}

data class WornItem(val id: ClothingItemId)
```

- ~~`WornItem`, а не `ClothingItemId`: позже сюда добавятся `tucked` и `attachedTo`, и API не придётся менять.~~
- ~~`save` не `suspend`: `worn` меняется сразу, запись идёт в фоне.~~
- ~~Репозиторий не проверяет id по датасету и не применяет правила. Он возвращает то, что сохранили.~~

### ~~4. Реализация~~

- ~~Формат файла:~~
  ```kotlin
  @Serializable
  internal data class OutfitFile(val version: Int = 1, val worn: List<StoredItem> = emptyList())

  @Serializable
  internal data class StoredItem(val category: String, val key: String)
  ```
  `version` нужен для будущих миграций. Неизвестные поля при чтении игнорируются (`ignoreUnknownKeys`).
- ~~`OutfitSerializer : Serializer<OutfitFile>`: `defaultValue = OutfitFile()`, ошибка разбора → `CorruptionException`.~~
- ~~`createOutfitDataStore(scope, file)`: `DataStoreFactory.create` с `ReplaceFileCorruptionHandler { OutfitFile() }`, по образцу `createThemeDataStore`.~~
- ~~`OutfitRepositoryImpl(dataStore, scope)`:~~
  - ~~чтение: `dataStore.data`, `IOException` → пустой образ, маппинг `StoredItem` ↔ `WornItem`;~~
  - ~~`save`: значение сразу в `worn`, затем в канал `CONFLATED`, один потребитель пишет на диск и глотает `IOException`;~~
  - ~~если `save` вызвали до первого чтения, прочитанное с диска значение его не затирает, как в `ThemeSelectorImpl`.~~
- ~~`outfitModule`: `single<DataStore<OutfitFile>>(named("outfit"))` с файлом `dataStoreFile("outfit.json")` на `Dispatchers.IO` и `single<OutfitRepository>`.~~

### ~~5. Подключение~~

- ~~`outfitModule` в `DressDollApplication`, зависимость `:app` → `:outfit:impl`.~~
- ~~Потребителей пока нет, первым станет `:doll:impl` в [части 2](plan_part_2.md).~~

### ~~6. Тесты `OutfitRepositoryImplTest`~~

По образцу `ThemeSelectorImplTest`:

- ~~`worn` равен `null` до чтения, затем пустой список, если файла нет;~~
- ~~сохранённый образ сразу виден в `worn` и читается с диска новым экземпляром;~~
- ~~порядок вещей сохраняется, дубликаты не убираются (это делает `normalize` в [части 2](plan_part_2.md));~~
- ~~из нескольких быстрых `save` побеждает последний;~~
- ~~повреждённый файл → пустой образ, его можно перезаписать;~~
- ~~сбой записи: образ живёт до конца сессии, приложение не падает;~~
- ~~сбой чтения: пустой образ, последующие `save` применяются;~~
- ~~`save` до первого чтения не затирается значением с диска;~~
- ~~файл с неизвестным полем читается.~~
