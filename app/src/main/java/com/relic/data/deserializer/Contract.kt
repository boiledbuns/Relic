package com.relic.data.deserializer

import com.relic.api.response.Listing
import com.relic.data.CommentsAndPostData
import com.relic.domain.exception.RelicException
import com.relic.domain.models.*
import com.relic.persistence.entities.AccountEntity

/**
 * Decoupled from the repository package because deserializers should be responsible
 * for directly converting server response into entities -> repo shouldn't care how it's done,
 * just that the correct results are returned
 */
interface Contract {

    interface PostDeserializer {
        /**
         * parse response that can contain both posts and comments
         */
        suspend fun parseListingItems(response: String) : Listing<ListingItem>

        suspend fun parsePosts(response: String) : Listing<PostModel>

        suspend fun parsePost(response: String) : PostModel
    }

    interface CommentDeserializer {
        /**
         * parses a post and its associated comments
         */
        suspend fun parseCommentsAndPost(response : String) : CommentsAndPostData

        /**
         * Only use this method to parse the return from "morechildren" since it uses a different
         * format than the traditional method for retrieving comments
         */
        suspend fun parseMoreCommentsResponse(
            moreChildrenComment: CommentModel,
            response : String
        ) : List<CommentModel>
    }

    interface UserDeserializer {
        suspend fun parseUser(userResponse: String, trophiesResponse : String) : UserModel

        suspend fun parseUsers(response: String) : Listing<UserModel>

        suspend fun parseUsername(response: String) : String
    }

    interface AccountDeserializer {
        suspend fun parseAccount(response : String) : AccountEntity
    }

    interface SubDeserializer {
        @Throws(RelicParseException::class)
        suspend fun parseSubredditResponse(response: String): SubredditModel
        suspend fun parseSubredditsResponse(response: String): Listing<SubredditModel>
        suspend fun parseSearchSubsResponse(response: String): List<SubPreviewModel>
    }

}

class RelicParseException(response : String, cause : Throwable? = null) : RelicException("error parsing response : `$response`", cause)