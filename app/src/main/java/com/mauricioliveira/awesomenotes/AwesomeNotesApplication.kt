package com.mauricioliveira.awesomenotes

import android.app.Application
import com.mauricioliveira.awesomenotes.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AwesomeNotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AwesomeNotesApplication)
            modules(appModules)
        }
    }
}
