package com.relic.dagger

import com.relic.dagger.modules.AppModule
import com.relic.presentation.editor.EditorVM
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component( modules = [AppModule::class] )
interface AppComponent {

    fun inject(repositoryComponent : RepositoryComponent)
}