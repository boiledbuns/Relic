package com.relic.data.mapper

import com.relic.data.entities.PostEntity
import com.relic.data.models.PostModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PostMapperImpl : PostMapper {
    override suspend fun toModel(postEntity: PostEntity): PostModel {
        return PostModel().apply {
            fullName = postEntity.name
        }
    }

    override suspend fun toModel(postEntities: List<PostEntity>): List<PostModel> {
        val iterator = postEntities.iterator()

        val deferredPostModels = ArrayList<Deferred<PostModel>>()

        coroutineScope {
            iterator.forEach { postEntity ->
                deferredPostModels.add(
                    async { toModel(postEntity) }
                )
            }
        }

        return deferredPostModels.awaitAll()
    }

}