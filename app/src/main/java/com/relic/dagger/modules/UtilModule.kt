package com.relic.dagger.modules

import android.app.Application
import com.gfycat.core.GfyCore
import com.relic.network.NetworkUtil
import com.relic.network.NetworkUtilImpl
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class UtilModule @Inject constructor(
    private val app : Application
) {

    @Provides
    @Singleton
    fun provideNetworkUtil() : NetworkUtil {
        return NetworkUtilImpl(app)
    }
}