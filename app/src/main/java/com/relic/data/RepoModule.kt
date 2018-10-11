package com.relic.data

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class RepoModule (
        private val applicationContext : Context) {

    @Provides
    @Singleton
    fun provideSubRepository() : SubRepository {
        return SubRepositoryImpl(applicationContext)
    }

    @Provides
    @Singleton
    fun providePostRepository() : PostRepository {
        return PostRepositoryImpl(applicationContext)
    }

    @Provides
    @Singleton
    fun provideCommentRepository() : CommentRepository {
        return CommentRepositoryImpl(applicationContext)
    }

    @Provides
    @Singleton
    fun provideListingRepository() : ListingRepository {
        return ListingRepositoryImpl(applicationContext)
    }
}