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
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepoModule (private val applicationContext : Context) {

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