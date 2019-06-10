package com.relic.dagger.modules

import android.app.Application
import android.arch.persistence.room.Room
import com.relic.data.ApplicationDB
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideDB(app: Application) : ApplicationDB {
        return Room.databaseBuilder(app, ApplicationDB::class.java, "relic.db").build()
    }

    @Singleton
    @Provides
    fun provideMoshi() : Moshi {
        return Moshi.Builder().build()
    }
}