package com.inventtrack

import android.app.Application
import com.inventtrack.di.AppContainer
import com.inventtrack.di.DefaultAppContainer

class InventTrackApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
