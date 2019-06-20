package com.relic.data.deserializer

import com.relic.api.response.Listing
import com.relic.data.CommentsAndPostData
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.SubSearchResult
import com.relic.data.entities.*
import com.relic.domain.models.CommentModel
import com.relic.domain.models.UserModel
import com.relic.domain.exception.RelicException
import com.relic.domain.models.PostModel
import org.json.simple.JSONObject

/**
 * Decoupled from the repository package because deserializers should be responsible
 * for directly converting server response into entities -> repo shouldn't care how it's done,
 * just that the correct results are returned
 */
interface Contract {

    interface PostDeserializer {
        suspend fun parsePosts(
            response: String,
            postSource: PostSource,
            listingKey : String
        ) : ParsedPostsData

        suspend fun parsePost(response: String) : ParsedPostData

        suspend fun parseSearchSubPostsResponse(response: String): SubSearchResult
    }

    interface CommentDeserializer {
        suspend fun parseCommentsAndPost(response : String) : CommentsAndPostData

        suspend fun parseCommentsResponse(
            postFullName: String,
            response: String
        ) : ParsedCommentData

        suspend fun parseMoreCommentsResponse(
            moreChildrenComment: CommentModel,
            response : String
        ) : List<CommentModel>

        fun removeTypePrefix(fullName : String) : String
    }

    interface UserDeserializer {
        suspend fun parseUser(userResponse: String, trophiesResponse : String) : UserModel

        suspend fun parseUsername(response: String) : String
    }

    interface AccountDeserializer {
        suspend fun parseAccount(response : String) : AccountEntity
    }

    interface SubDeserializer {
        suspend fun parseSubredditResponse(response: String): SubredditEntity
        suspend fun parseSubredditsResponse(response: String): ParsedSubsData
        suspend fun parseSearchSubsResponse(response: String): List<String>
    }

}

data class ParsedPostData(
    val postSourceEntity:PostSourceEntity,
    val postEntity : PostEntity
)

data class ParsedPostsData(
    val postSourceEntities:List<PostSourceEntity>,
    val postEntities : List<PostEntity>,
    val commentEntities : List<CommentModel>,
    val listingEntity: ListingEntity
)

data class ParsedCommentData(
    val listingEntity : ListingEntity,
    val commentList : List<CommentModel>,
    val replyCount : Int
)

data class ParsedSubsData(
    val subsList : List<SubredditEntity>,
    val after : String?
)

class RelicParseException(response : String, cause : Throwable? = null) : RelicException("error parsing response : `$response`", cause)