package com.relic.data.deserializer

import com.relic.data.PostRepository
import com.relic.data.entities.*
import com.relic.data.models.CommentModel
import com.relic.data.models.UserModel
import com.relic.exception.RelicException
import org.json.simple.JSONObject

interface Contract {

    interface PostDeserializer {
        suspend fun parsePosts(
            response: String,
            postSource: PostRepository.PostSource,
            listingKey : String
        ) : ParsedPostsData

        suspend fun parsePost(response: String) : ParsedPostData
    }

    interface CommentDeserializer {
        suspend fun parseCommentsResponse(
            postFullName: String,
            response: String
        ) : ParsedCommentData

        suspend fun parseMoreCommentsResponse(
            moreChildrenComment: CommentModel,
            response : String
        ) : List<CommentEntity>

        suspend fun unmarshallComment(
            commentChild : JSONObject,
            commentPosition : Float
        ) : List<CommentEntity>
    }

    interface UserDeserializer {
        suspend fun parseUser(
            userResponse: String, trophiesResponse : String) : UserModel
    }

    interface AccountDeserializer {
        suspend fun parseAccount(accountResponse : String) : AccountEntity
    }

    interface SubDeserializer {
        suspend fun parseSubreddits(response: String): List<SubredditEntity>
        suspend fun parseSearchedSubs(response: String): List<String>
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

data class ParsedCommentData(
    val listingEntity : ListingEntity,
    val commentList : List<CommentEntity>,
    val replyCount : Int
)

class DeserializationException(message : String, cause : Throwable) : RelicException(message, cause)