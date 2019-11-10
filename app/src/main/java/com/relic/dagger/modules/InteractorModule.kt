package com.relic.dagger.modules

import com.relic.interactor.CommentInteractorImpl
import com.relic.interactor.Contract
import com.relic.interactor.PostInteractorImpl
import com.relic.interactor.SubredditInteractorImpl
import dagger.Binds
import dagger.Module

@Suppress("unused")
@Module
abstract class InteractorModule {

    @Binds
    abstract fun bindPostInteractor(postInteractor: PostInteractorImpl) : Contract.PostAdapterDelegate

    @Binds
    abstract fun bindSubredditInteractor(subredditInteractor: SubredditInteractorImpl) : Contract.SubAdapterDelegate

    @Binds
    abstract fun bindCommentInteractor(commentInteractor: CommentInteractorImpl) : Contract.CommentAdapterDelegate
}