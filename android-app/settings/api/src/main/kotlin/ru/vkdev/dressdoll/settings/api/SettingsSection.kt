package ru.vkdev.dressdoll.settings.api

import androidx.compose.foundation.lazy.LazyListScope

/** Фича регистрирует свой раздел в Koin, экран настроек собирает все разделы через `getAll()`. */
interface SettingsSection {
    /** Меньше — выше на экране. */
    val order: Int

    /** Ключи элементов уникальны в пределах всего экрана: начинаются с имени раздела. */
    fun LazyListScope.content()
}
