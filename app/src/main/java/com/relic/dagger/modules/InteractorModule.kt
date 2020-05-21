package com.relic.dagger.modules

import com.relic.interactor.*
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

    @Binds
    abstract fun bindUserInteractor(userInteractorImpl: UserInteractorImpl) : Contract.UserAdapterDelegate
}