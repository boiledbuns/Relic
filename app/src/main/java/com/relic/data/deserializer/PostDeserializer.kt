package com.relic.data.deserializer

import com.relic.data.PostRepository
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.data.entities.PostEntity
import com.relic.data.entities.PostSourceEntity

interface PostDeserializer {
    suspend fun parsePost(response: String) : PostEntity

    suspend fun parsePosts(
        response: String,
        postSource: PostRepository.PostSource,
        listingKey : String
    ) : ParsedPostsData
}

data class ParsedPostsData(
    val postSourceEntities:List<PostSourceEntity>,
    val postEntities : List<PostEntity>,
    val commentEntities : List<CommentEntity>,
    val listingEntity: ListingEntity
)