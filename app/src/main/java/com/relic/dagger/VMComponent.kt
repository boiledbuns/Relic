package com.relic.dagger

import android.arch.lifecycle.ViewModelProvider
import com.relic.presentation.main.MainVM
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.UtilModule
import com.relic.presentation.displaypost.DisplayPostVM
import com.relic.presentation.displaysub.DisplaySubVM
import com.relic.presentation.displaysubs.DisplaySubsVM
import com.relic.presentation.displayuser.DisplayUserVM
import com.relic.presentation.editor.EditorViewModel
import com.relic.presentation.home.frontpage.FrontpageVM
import com.relic.presentation.subinfodialog.SubInfoDialogVM
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component( modules = [RepoModule::class, AuthModule::class, UtilModule::class])
interface VMComponent {

    fun getMainVM() : MainVM.Factory

    fun getDisplayPostVM() : DisplayPostVM.Factory

    fun getEditorVM() : EditorViewModel.Factory

    fun getDisplaySubInfoVM() : SubInfoDialogVM.Factory

    fun injectEditor(factory: ViewModelProvider.Factory)

    fun getDisplaySubsVM() : DisplaySubsVM.Factory

    fun getDisplaySubVM() : DisplaySubVM.Factory

    fun getDisplayFrontpageVM() : FrontpageVM.Factory

    fun getDisplayUserVM() : DisplayUserVM.Factory
}