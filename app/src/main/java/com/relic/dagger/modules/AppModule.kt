package com.relic.dagger.modules

import android.app.Application
import com.relic.persistence.ApplicationDB
import com.relic.persistence.TypeConverters
import com.relic.data.deserializer.Deserializer
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
            TypeConverters.moshi = moshi
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
}