package com.souschef

import android.app.Application
import com.souschef.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for SousChef.
 * Initializes Koin dependency injection with all app modules.
 */
class SousChefApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Log Koin events (use Level.ERROR in production)
            androidLogger(Level.DEBUG)

            // Android context
            androidContext(this@SousChefApplication)

            // Load modules
            modules(appModules)
        }
    }
}
