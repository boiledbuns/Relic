package com.relic.dagger.modules

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.relic.data.ApplicationDB
import com.relic.data.UserRepository
import com.relic.data.UserRepositoryImpl
import dagger.Binds
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
}