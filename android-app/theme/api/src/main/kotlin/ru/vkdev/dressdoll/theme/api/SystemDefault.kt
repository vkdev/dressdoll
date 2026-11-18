package ru.vkdev.dressdoll.theme.api

/** Роль палитры в выборе «Как в системе»: её берут при светлой или тёмной теме системы. */
enum class SystemDefault {
    /** Ещё и палитра по умолчанию, пока пользователь ничего не выбрал. */
    LIGHT,
    DARK,
}
