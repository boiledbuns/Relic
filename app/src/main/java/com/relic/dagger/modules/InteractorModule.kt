package com.relic.dagger.modules

import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.PostInteractor
import dagger.Binds
import dagger.Module

@Suppress("unused")
@Module
abstract class InteractorModule {

    @Binds
    abstract fun bindPostInteractor(postAdapterDelegate: PostInteractor) : DisplaySubContract.PostAdapterDelegate
}