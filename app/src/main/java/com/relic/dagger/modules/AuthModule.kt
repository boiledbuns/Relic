package com.relic.dagger.modules

import android.content.Context
import com.relic.data.Authenticator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AuthModule(private val applicationContext : Context) {

    @Provides
    @Singleton
    fun provideAuthModule() : Authenticator {
        return Authenticator(applicationContext)
    }
}

