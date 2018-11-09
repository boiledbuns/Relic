package com.relic.dagger.modules

import android.content.Context
import com.relic.data.CommentRepository
import com.relic.data.CommentRepositoryImpl
import com.relic.data.ListingRepository
import com.relic.data.ListingRepositoryImpl
import com.relic.data.PostRepository
import com.relic.data.PostRepositoryImpl
import com.relic.data.SubRepository
import com.relic.data.SubRepositoryImpl
import com.relic.network.NetworkRequestManager
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class RepoModule @Inject constructor(
    private val applicationContext : Context
) {

    private val networkRequestManager: NetworkRequestManager = NetworkRequestManager(applicationContext)

    @Provides
    @Singleton
    fun provideSubRepository() : SubRepository {
        return SubRepositoryImpl(applicationContext, networkRequestManager)
    }

    @Provides
    @Singleton
    fun providePostRepository() : PostRepository {
        return PostRepositoryImpl(applicationContext, networkRequestManager)
    }

    @Provides
    @Singleton
    fun provideCommentRepository() : CommentRepository {
        return CommentRepositoryImpl(applicationContext, networkRequestManager, provideListingRepository())
    }

    @Provides
    @Singleton
    fun provideListingRepository() : ListingRepository {
        return ListingRepositoryImpl(applicationContext)
    }
}