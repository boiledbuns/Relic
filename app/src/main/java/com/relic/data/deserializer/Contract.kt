package com.relic.data.deserializer

import com.relic.data.PostRepository
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.data.entities.PostEntity
import com.relic.data.entities.PostSourceEntity
import com.relic.data.models.CommentModel
import com.relic.data.models.UserModel
import org.json.simple.JSONObject

interface Contract {

    interface PostDeserializer {
        suspend fun parsePost(response: String) : ParsedPostData

        suspend fun parsePosts(
            response: String,
            postSource: PostRepository.PostSource,
            listingKey : String
        ) : ParsedPostsData
    }

    interface CommentDeserializer {
        suspend fun parseComments(
            postFullName: String,
            response: JSONObject,
            parentDepth : Int = -1,
            parentPosition : Float = 0f
        ) : ParsedCommentData

        suspend fun parseMoreComments(
            moreChildrenComment: CommentModel,
            requestJson: JSONObject
        ) : List<CommentEntity>

        suspend fun unmarshallComment(
            commentChild : JSONObject,
            commentPosition : Float
        ) : List<CommentEntity>
    }


    interface UserDeserializer {
        suspend fun parseUser(userResponse: String, trophiesResponse : String) : UserModel
    }

}

data class ParsedPostData(
    val postSourceEntity:PostSourceEntity,
    val postEntity : PostEntity
)

data class ParsedPostsData(
    val postSourceEntities:List<PostSourceEntity>,
    val postEntities : List<PostEntity>,
    val commentEntities : List<CommentEntity>,
    val listingEntity: ListingEntity
)
