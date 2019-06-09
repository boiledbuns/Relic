package com.relic.dagger.modules

import com.relic.network.NetworkUtil
import com.relic.network.NetworkUtilImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Suppress("unused")
@Module
abstract class UtilModule {

    @Binds
    @Singleton
    abstract fun bindNetworkUtil(networkUtilImpl: NetworkUtilImpl) : NetworkUtil
}