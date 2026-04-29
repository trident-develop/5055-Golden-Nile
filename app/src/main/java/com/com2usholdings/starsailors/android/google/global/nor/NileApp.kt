package com.com2usholdings.starsailors.android.google.global.nor

import android.app.Application
import com.com2usholdings.starsailors.android.google.global.nor.di.dataStoreModule
import com.com2usholdings.starsailors.android.google.global.nor.di.gameModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class NileApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NileApp)
            modules(
                dataStoreModule,
                gameModule
            )
        }
    }
}