package ru.vkdev.dressdoll

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.vkdev.dressdoll.catalog.impl.catalogModule
import ru.vkdev.dressdoll.doll.impl.dollModule
import ru.vkdev.dressdoll.navigation.impl.navigationModule
import ru.vkdev.dressdoll.outfit.impl.outfitModule
import ru.vkdev.dressdoll.repository.api.DollRepository
import ru.vkdev.dressdoll.repository.impl.repositoryModule
import ru.vkdev.dressdoll.settings.theme.impl.themeSettingsModule
import ru.vkdev.dressdoll.theme.impl.themeModule
import ru.vkdev.dressdoll.theme.palette.avocado.avocadoPaletteModule
import ru.vkdev.dressdoll.theme.palette.cinnamon.cinnamonPaletteModule
import ru.vkdev.dressdoll.theme.palette.denim.denimPaletteModule
import ru.vkdev.dressdoll.theme.palette.disco.discoPaletteModule
import ru.vkdev.dressdoll.theme.palette.powder.powderPaletteModule

class DressDollApplication : Application() {

    private val repository: DollRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DressDollApplication)
            modules(
                themeModule,
                cinnamonPaletteModule,
                discoPaletteModule,
                avocadoPaletteModule,
                powderPaletteModule,
                denimPaletteModule,
                navigationModule,
                repositoryModule,
                outfitModule,
                catalogModule,
                dollModule,
                themeSettingsModule,
            )
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
