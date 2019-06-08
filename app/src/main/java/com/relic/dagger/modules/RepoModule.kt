package com.relic.dagger.modules

import com.relic.data.*
import dagger.Binds
import dagger.Module

@Module
abstract class RepoModule {
    @Binds
    abstract fun bindSubRepository(subRepo : SubRepositoryImpl) : SubRepository

    @Binds
    abstract fun bindPostRepository(postRepo : PostRepositoryImpl) : PostRepository

    @Binds
    abstract fun bindCommentRepository(commentRepo : CommentRepositoryImpl) : CommentRepository

    @Binds
    abstract fun bindListingRepository(listingRepo : ListingRepositoryImpl) : ListingRepository

    @Binds
    abstract fun bindUserRepository(userRepo : UserRepositoryImpl) : UserRepository

//    @Provides
//    @Singleton
//    fun provideSubRepository() : SubRepository {
//        return SubRepositoryImpl(app, networkRequestManager)
//    }
//
//    @Provides
//    @Singleton
//    fun providePostRepository(postDeserializer: Contract.PostDeserializer) : PostRepository {
//        return PostRepositoryImpl(app, networkRequestManager, appDB, postDeserializer)
//    }
//
//    @Provides
//    @Singleton
//    fun provideCommentRepository(listingRepo: ListingRepository, commentDeserializer: CommentDeserializer) : CommentRepository {
//        return CommentRepositoryImpl(networkRequestManager, appDB, listingRepo, commentDeserializer)
//    }
//
//    @Provides
//    @Singleton
//    fun provideListingRepository() : ListingRepository {
//        return ListingRepositoryImpl(app)
//    }
//
//    @Provides
//    @Singleton
//    fun provideUserRepository() : UserRepository {
//        return UserRepositoryImpl(app, networkRequestManager, UserDeserializerImpl(), AccountDeserializerImpl())
//    }
}