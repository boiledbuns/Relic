package com.relic.dagger.modules

import com.relic.presentation.displaysubs.DisplaySubsView
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Just a note for future reference
 * the `@ContributesAndroidInjector` annotation tells dagger the dep. should be part of the dep.
 * graph + should be used with the `dagger.android` classes
 */
@Module
abstract class FragBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeDisplaySubsFragment() : DisplaySubsView
}