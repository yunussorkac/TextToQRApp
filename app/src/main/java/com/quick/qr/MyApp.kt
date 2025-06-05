package com.quick.qr

import android.app.Application
import com.quick.qr.di.Modules.repositoryModule
import com.quick.qr.di.Modules.roomModule
import com.quick.qr.di.Modules.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {


    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(
                viewModelModule,
                repositoryModule,
                roomModule
            )

        }
    }
}