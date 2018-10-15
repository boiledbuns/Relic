package com.relic.dagger

import android.arch.lifecycle.ViewModelProvider
import com.relic.data.RepoModule
import com.relic.presentation.displaypost.DisplayPostVM
import com.relic.presentation.editor.EditorVM
import com.relic.presentation.subinfodialog.SubInfoDialogVM
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component( modules = [RepoModule::class])
interface VMComponent {

    fun getDisplayPostVM() : DisplayPostVM.Factory

    fun getEditorVM() : EditorVM.Factory

    fun getDisplaySubInfoVM() : SubInfoDialogVM.Factory

    fun injectEditor(factory: ViewModelProvider.Factory)
}