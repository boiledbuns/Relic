package com.relic.dagger

import android.content.Context
import com.relic.dagger.modules.AppModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component( modules = [AppModule::class] )
interface AppComponent {

    fun inject(repositoryComponent : RepositoryComponent)
}