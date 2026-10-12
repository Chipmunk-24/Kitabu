package com.stadiolinks.kitabu

import android.app.Application
import com.stadiolinks.kitabu.di.AppContainer

class KitabuApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
