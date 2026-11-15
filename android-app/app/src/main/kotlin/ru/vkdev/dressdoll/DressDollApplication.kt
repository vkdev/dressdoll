package ru.vkdev.dressdoll

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.vkdev.dressdoll.doll.impl.dollModule
import ru.vkdev.dressdoll.navigation.impl.navigationModule
import ru.vkdev.dressdoll.repository.api.DollRepository
import ru.vkdev.dressdoll.repository.impl.repositoryModule
import ru.vkdev.dressdoll.theme.impl.themeModule

class DressDollApplication : Application() {

    private val repository: DollRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DressDollApplication)
            modules(themeModule, navigationModule, repositoryModule, dollModule)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        repository.onTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        repository.onLowMemory()
    }
}
