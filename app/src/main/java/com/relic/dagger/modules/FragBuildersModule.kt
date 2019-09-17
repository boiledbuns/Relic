package com.relic.dagger.modules

import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaypost.tabs.CommentsFragment
import com.relic.presentation.displaypost.tabs.FullPostFragment
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.search.posts.PostsSearchFragment
import com.relic.presentation.displaysubs.DisplaySubsFragment
import com.relic.presentation.displayuser.DisplayUserFragment
import com.relic.presentation.displayuser.DisplayUserPreview
import com.relic.presentation.displayuser.fragments.PostsTabFragment
import com.relic.presentation.editor.NewPostEditorFragment
import com.relic.presentation.editor.ReplyEditorFragment
import com.relic.presentation.home.HomeFragment
import com.relic.presentation.home.frontpage.FrontpageFragment
import com.relic.presentation.login.SignInFragment
import com.relic.presentation.media.DisplayImageFragment
import com.relic.presentation.preferences.PreferencesFragment
import com.relic.presentation.preferences.appearance.PostLayoutFragment
import com.relic.presentation.preferences.appearance.ThemeFragment
import com.relic.presentation.search.posts.PostsSearchResultsFragment
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
    abstract fun contributeDisplaySubs() : DisplaySubsFragment

    @ContributesAndroidInjector
    abstract fun contributeHome() : HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeFrontpage() : FrontpageFragment

    @ContributesAndroidInjector
    abstract fun contributeSub() : DisplaySubFragment

    @ContributesAndroidInjector
    abstract fun contributePost() : DisplayPostFragment

    @ContributesAndroidInjector
    abstract fun contributesTabCommentsFragment() : CommentsFragment

    @ContributesAndroidInjector
    abstract fun contributesTabFullPostFragment() : FullPostFragment

    @ContributesAndroidInjector
    abstract fun contributeUser() : DisplayUserFragment

    @ContributesAndroidInjector
    abstract fun contributeUserTabFragment() : PostsTabFragment

    @ContributesAndroidInjector
    abstract fun contributeNewPostEditor() : NewPostEditorFragment

    @ContributesAndroidInjector
    abstract fun contributeReplyEditor() : ReplyEditorFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPreview() : DisplayUserPreview

    @ContributesAndroidInjector
    abstract fun contributeSubInfo() : SubInfoBottomSheetDialog

    @ContributesAndroidInjector
    abstract fun contributePostsSearchFragment() : PostsSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeDisplayMedia() : DisplayImageFragment

    @ContributesAndroidInjector
    abstract fun postsSearchResults() : PostsSearchResultsFragment

    // endregion main ui

    @ContributesAndroidInjector
    abstract fun contributeLogin() : SignInFragment

    @ContributesAndroidInjector
    abstract fun contributePreferences() : PreferencesFragment

    @ContributesAndroidInjector
    abstract fun contributeTheme() : ThemeFragment

    @ContributesAndroidInjector
    abstract fun contributePostLayout() : PostLayoutFragment
}