package com.relic.data.mapper

import com.relic.data.entities.PostEntity
import com.relic.data.models.PostModel

interface PostMapper {

    suspend fun toModel(postEntity: PostEntity) : PostModel

    suspend fun toModel(postEntities : List<PostEntity>) : List<PostModel>
}