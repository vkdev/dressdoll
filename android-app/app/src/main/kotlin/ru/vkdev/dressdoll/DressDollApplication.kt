package ru.vkdev.dressdoll

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.vkdev.dressdoll.doll.impl.dollModule
import ru.vkdev.dressdoll.repository.impl.repositoryModule
import ru.vkdev.dressdoll.theme.impl.themeModule

class DressDollApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DressDollApplication)
            modules(themeModule, repositoryModule, dollModule)
        }
    }
}
