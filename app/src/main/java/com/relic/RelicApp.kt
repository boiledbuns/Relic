package com.relic

import android.app.Application
import com.relic.dagger.AppComponent
import com.relic.dagger.DaggerAppComponent
import com.relic.dagger.modules.AuthModule

class RelicApp : Application() {

    lateinit var appComponent : AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .authModule(AuthModule(applicationContext))
                .build()
    }

}