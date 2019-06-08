package com.relic.dagger.modules

import com.relic.presentation.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [FragBuildersModule::class])
    abstract fun contributeMainActivity() : MainActivity
}