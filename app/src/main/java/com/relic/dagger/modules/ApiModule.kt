package com.relic.dagger.modules

import com.relic.data.gateway.*
import dagger.Binds
import dagger.Module

@Suppress("unused")
@Module
abstract class ApiModule {
    @Binds
    abstract fun bindSubApi(subGateway : SubGatewayImpl) : SubGateway

    @Binds
    abstract fun bindPostApi(postGateway : PostGatewayImpl) : PostGateway

    @Binds
    abstract fun bindCommentApi(commentGateway : CommentGatewayImpl) : CommentGateway

    @Binds
    abstract fun bindUserApi(userGateway : UserGatewayImpl) : UserGateway
}