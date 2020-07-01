package com.relic.dagger.modules

import com.relic.network.NetworkUtil
import com.relic.network.NetworkUtilImpl
import com.relic.scheduler.ScheduleManager
import com.relic.scheduler.ScheduleManagerImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Suppress("unused")
@Module
abstract class UtilModule {

    @Binds
    abstract fun bindNetworkUtil(networkUtilImpl: NetworkUtilImpl) : NetworkUtil

    @Binds
    @Singleton
    abstract fun bindScheduleManager(scheduleManagerImpl: ScheduleManagerImpl) : ScheduleManager
}