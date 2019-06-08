package com.relic.dagger.modules

import com.relic.presentation.handler.LinkHandlerActivity
import com.relic.presentation.login.LoginActivity
import com.relic.presentation.main.MainActivity
import com.relic.presentation.preferences.PreferencesActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [FragBuildersModule::class])
    abstract fun contributeMainActivity() : MainActivity

    @ContributesAndroidInjector(modules = [FragBuildersModule::class])
    abstract fun contributeLoginActivity() : LoginActivity

    @ContributesAndroidInjector(modules = [FragBuildersModule::class])
    abstract fun contributePreferencesActivity() : PreferencesActivity

    @ContributesAndroidInjector(modules = [FragBuildersModule::class])
    abstract fun contributeLinkHandlerActivity() : LinkHandlerActivity
}