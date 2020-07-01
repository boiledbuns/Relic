package com.relic.dagger.modules

import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.relic.persistence.ApplicationDB
import com.relic.persistence.RoomTypeConverters
import com.relic.data.deserializer.Deserializer
import com.relic.preference.ViewPreferencesManager
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.json.simple.parser.JSONParser
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideDB(app: Application, moshi : Moshi) : ApplicationDB {
        return ApplicationDB.getDatabase(app).apply {
            // set the moshi adapter here bc we cannot inject moshi into type converter class
            RoomTypeConverters.moshi = moshi
        }
    }

    @Singleton
    @Provides
    fun provideMoshi() : Moshi {
        return Deserializer.getInstance()
    }

    @Singleton
    @Provides
    fun provideJSONParser() : JSONParser{
        return JSONParser()
    }

    @Singleton
    @Provides
    fun provideViewPreferencesManager(app: Application) : ViewPreferencesManager {
        return ViewPreferencesManager(app)
    }

    @Singleton
    @Provides
    fun provideVolleyQueue(app: Application) : RequestQueue {
        return Volley.newRequestQueue(app.applicationContext)
    }
}