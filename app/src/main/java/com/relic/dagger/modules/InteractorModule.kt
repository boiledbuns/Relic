package com.relic.dagger.modules

import com.relic.presentation.displaypost.CommentInteractor
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.PostInteractor
import com.relic.presentation.displaysubs.DisplaySubsContract
import com.relic.presentation.displaysubs.SubredditInteractor
import dagger.Binds
import dagger.Module

@Suppress("unused")
@Module
abstract class InteractorModule {

    @Binds
    abstract fun bindPostInteractor(postInteractor: PostInteractor) : DisplaySubContract.PostAdapterDelegate

    @Binds
    abstract fun bindSubredditInteractor(subredditInteractor: SubredditInteractor) : DisplaySubsContract.SubAdapterDelegate

    @Binds
    abstract fun bindCommentInteractor(commentInteractor: CommentInteractor) : DisplayPostContract.CommentAdapterDelegate
}