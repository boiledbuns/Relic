package com.relic.dagger.modules

import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.displaysubs.DisplaySubsView
import com.relic.presentation.displayuser.DisplayUserFragment
import com.relic.presentation.displayuser.DisplayUserPreview
import com.relic.presentation.editor.NewPostEditorFragment
import com.relic.presentation.editor.ReplyEditorFragment
import com.relic.presentation.home.HomeFragment
import com.relic.presentation.home.frontpage.FrontpageFragment
import com.relic.presentation.login.SignInFragment
import com.relic.presentation.preferences.PreferencesFragment
import com.relic.presentation.preferences.appearance.ThemeFragment
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Just a note for future reference
 * the `@ContributesAndroidInjector` annotation tells dagger the dep. should be part of the dep.
 * graph + should be used with the `dagger.android` classes
 */
@Suppress("unused")
@Module
abstract class FragBuildersModule {

    // region main ui

    @ContributesAndroidInjector
    abstract fun contributeDisplaySubs() : DisplaySubsView

    @ContributesAndroidInjector
    abstract fun contributeHome() : HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeFrontpage() : FrontpageFragment

    @ContributesAndroidInjector
    abstract fun contributeSub() : DisplaySubFragment

    @ContributesAndroidInjector
    abstract fun contributePost() : DisplayPostFragment

    @ContributesAndroidInjector
    abstract fun contributeUser() : DisplayUserFragment

    @ContributesAndroidInjector
    abstract fun contributeNewPostEditor() : NewPostEditorFragment

    @ContributesAndroidInjector
    abstract fun contributeReplyEditor() : ReplyEditorFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPreview() : DisplayUserPreview

    @ContributesAndroidInjector
    abstract fun contributeSubInfo() : SubInfoBottomSheetDialog

    // endregion main ui

    @ContributesAndroidInjector
    abstract fun contributeLogin() : SignInFragment

    @ContributesAndroidInjector
    abstract fun contributePreferences() : PreferencesFragment

    @ContributesAndroidInjector
    abstract fun contributeTheme() : ThemeFragment
}