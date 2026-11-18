package ru.vkdev.dressdoll

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.vkdev.dressdoll.catalog.impl.catalogModule
import ru.vkdev.dressdoll.clothing.impl.clothingModule
import ru.vkdev.dressdoll.core.coroutines.coroutinesModule
import ru.vkdev.dressdoll.disco.impl.discoModule
import ru.vkdev.dressdoll.doll.impl.dollModule
import ru.vkdev.dressdoll.navigation.impl.navigationModule
import ru.vkdev.dressdoll.outfit.impl.outfitModule
import ru.vkdev.dressdoll.settings.impl.settingsModule
import ru.vkdev.dressdoll.theme.impl.themeModule
import ru.vkdev.dressdoll.theme.palette.avocado.avocadoPaletteModule
import ru.vkdev.dressdoll.theme.palette.cinnamon.cinnamonPaletteModule
import ru.vkdev.dressdoll.theme.palette.denim.denimPaletteModule
import ru.vkdev.dressdoll.theme.palette.disco.discoPaletteModule
import ru.vkdev.dressdoll.theme.palette.powder.powderPaletteModule

class DressDollApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DressDollApplication)
            modules(
                coroutinesModule,
                themeModule,
                cinnamonPaletteModule,
                discoPaletteModule,
                avocadoPaletteModule,
                powderPaletteModule,
                denimPaletteModule,
                navigationModule,
                clothingModule,
                outfitModule,
                discoModule,
                catalogModule,
                dollModule,
                settingsModule,
            )
        }
    }
}
