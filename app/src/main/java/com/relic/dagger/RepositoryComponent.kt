package com.relic.dagger

import com.relic.dagger.modules.RepoModule
import com.relic.presentation.editor.NewPostEditorFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RepoModule::class])
interface RepositoryComponent {

    fun inject(editorView: NewPostEditorFragment)

//    fun buildEditorVM() : EditorViewModel
}