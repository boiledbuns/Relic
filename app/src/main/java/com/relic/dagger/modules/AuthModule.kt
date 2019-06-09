package com.relic.dagger.modules

import com.relic.data.Auth
import com.relic.data.auth.AuthDeserializerImpl
import com.relic.data.auth.AuthImpl
import dagger.Binds
import dagger.Module

@Suppress("unused")
@Module
abstract class AuthModule {

    @Binds
    abstract fun bindAuth(auth : AuthImpl) : Auth

    @Binds
    abstract fun bindAuthDeserializer(authDeserializer: AuthDeserializerImpl) : Auth.Deserializer
}

