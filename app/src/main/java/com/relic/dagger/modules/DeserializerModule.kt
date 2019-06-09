package com.relic.dagger.modules

import com.relic.data.deserializer.*
import dagger.Binds
import dagger.Module

@Suppress("unused")
@Module
abstract class DeserializerModule {
    @Binds
    abstract fun bindSubDeserializer(subDeserializerImpl: SubDeserializerImpl) : Contract.SubDeserializer

    @Binds
    abstract fun bindPostDeserializer(postDeserializerImpl_Factory: PostDeserializerImpl) : Contract.PostDeserializer

    @Binds
    abstract fun bindCommentDeserializer(commentDeserializerImpl : CommentDeserializerImpl) : Contract.CommentDeserializer

    @Binds
    abstract fun bindUserDeserializer(userDeserializerImpl: UserDeserializerImpl) : Contract.UserDeserializer

    @Binds
    abstract fun bindAccountDeserializer(accountDeserializerImpl: AccountDeserializerImpl) : Contract.AccountDeserializer
}