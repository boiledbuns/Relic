package com.relic.dagger.modules

import android.content.Context
import com.relic.data.*
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

    @Provides
    @Singleton
    fun provideUserRepository() : UserRepository {
        return UserRepositoryImpl(applicationContext, networkRequestManager)
    }
}