package com.relic.dagger.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule (private val applicationContext : Context){

    @Provides
    @Singleton
    fun provideAppContext() : Context{
        return applicationContext
    }

}