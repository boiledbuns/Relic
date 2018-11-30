package com.relic.dagger

import android.arch.lifecycle.ViewModelProvider
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.presentation.displaypost.DisplayPostVM
import com.relic.presentation.displaysub.DisplaySubVM
import com.relic.presentation.displaysubs.DisplaySubsVM
import com.relic.presentation.editor.EditorVM
import com.relic.presentation.home.frontpage.FrontpageVM
import com.relic.presentation.subinfodialog.SubInfoDialogVM
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component( modules = [RepoModule::class, AuthModule::class])
interface VMComponent {

    fun getDisplayPostVM() : DisplayPostVM.Factory

    fun getEditorVM() : EditorVM.Factory

    fun getDisplaySubInfoVM() : SubInfoDialogVM.Factory

    fun injectEditor(factory: ViewModelProvider.Factory)

    fun getDisplaySubsVM() : DisplaySubsVM.Factory

    fun getDisplaySubVM() : DisplaySubVM.Factory

    fun getDisplayFrontpageVM() : FrontpageVM.Factory
}