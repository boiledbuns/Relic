package com.relic.dagger.modules

import com.relic.data.*
import dagger.Binds
import dagger.Module

@Suppress("unused")
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
}