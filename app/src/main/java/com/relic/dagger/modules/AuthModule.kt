package com.relic.dagger.modules

import android.content.Context
import com.relic.data.auth.AuthImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AuthModule (private val applicationContext : Context) {

    @Provides
    @Singleton
    fun provideAuthModule() : AuthImpl {
        return AuthImpl(applicationContext)
    }
}

