package com.relic.dagger.modules

import android.app.Application
import com.relic.data.*
import com.relic.data.deserializer.AccountDeserializerImpl
import com.relic.data.deserializer.CommentDeserializer
import com.relic.data.deserializer.Contract
import com.relic.data.deserializer.UserDeserializerImpl
import com.relic.network.NetworkRequestManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class RepoModule {
    @Inject lateinit var app: Application
    @Inject lateinit var networkRequestManager: NetworkRequestManager
    @Inject lateinit var appDB: ApplicationDB

    @Provides
    @Singleton
    fun provideSubRepository() : SubRepository {
        return SubRepositoryImpl(app, networkRequestManager)
    }

    @Provides
    @Singleton
    fun providePostRepository(postDeserializer: Contract.PostDeserializer) : PostRepository {
        return PostRepositoryImpl(app, networkRequestManager, appDB, postDeserializer)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(listingRepo: ListingRepository, commentDeserializer: CommentDeserializer) : CommentRepository {
        return CommentRepositoryImpl(networkRequestManager, appDB, listingRepo, commentDeserializer)
    }

    @Provides
    @Singleton
    fun provideListingRepository() : ListingRepository {
        return ListingRepositoryImpl(app)
    }

    @Provides
    @Singleton
    fun provideUserRepository() : UserRepository {
        return UserRepositoryImpl(app, networkRequestManager, UserDeserializerImpl(), AccountDeserializerImpl())
    }
}