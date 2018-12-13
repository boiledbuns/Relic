package com.relic.dagger

import com.relic.dagger.modules.RepoModule
import com.relic.presentation.editor.EditorView
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RepoModule::class])
interface RepositoryComponent {

    fun inject(editorView: EditorView)

//    fun buildEditorVM() : EditorViewModel
}