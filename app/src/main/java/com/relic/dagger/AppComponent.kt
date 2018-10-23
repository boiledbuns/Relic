package com.relic.dagger

import com.relic.MainActivity
import com.relic.dagger.modules.AppModule
import com.relic.dagger.modules.AuthModule
import com.relic.data.Authenticator
import com.relic.presentation.editor.EditorVM
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component( modules = [AppModule::class, AuthModule::class] )
interface AppComponent {

    fun inject(mainActivity: MainActivity)

    fun inject(repositoryComponent : RepositoryComponent)
}