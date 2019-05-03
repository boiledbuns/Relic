package com.relic

import android.app.Application
import com.gfycat.core.GfyCoreInitializationBuilder
import com.gfycat.core.GfyCoreInitializer
import com.gfycat.core.GfycatApplicationInfo
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

        // opted not to provide GfyCore instance through dagger di since it's already a singleton
        GfyCoreInitializer.initialize(
            GfyCoreInitializationBuilder(this, GfycatApplicationInfo(
                getString(R.string.gfycat_client_id),
                getString(R.string.gfycat_client_secret)
            ))
        )
    }

}