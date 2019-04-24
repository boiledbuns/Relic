package com.relic.data

import android.arch.lifecycle.LiveData
import android.os.Parcelable

import com.relic.data.gateway.PostGateway
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.data.models.PostModel
import com.relic.network.request.RelicRequestError
import kotlinx.android.parcel.Parcelize

interface PostRepository {
    /**
     * Exposes gateway for vm to interact with objects stored on the server
     * @return gateway object for exposing post actions
     */
    val postGateway: PostGateway

    /**
     * Exposes the livedata list of posts
     * @param postSource subreddit to get the list for
     * @return live data list of posts
     */
    fun getPosts(postSource: PostSource): LiveData<List<PostModel>>

    /**
     * Retrieves posts for a subreddit
     * @param postSource origin of the post
     * @param listingAfter after value associated with the listing of the current set of posts
     */
    suspend fun retrieveMorePosts(postSource: PostSource, listingAfter: String)

    /**
     *
     * @param callback
     * @param subName
     */
    fun getNextPostingVal(callback: RetrieveNextListingCallback, postSource: PostSource)

    /**
     * exposes a single post model as livedata
     * @param postFullName a valid "full name" for a post
     * @return a model of the post wrapped in Livedata
     */
    fun getPost(postFullName: String): LiveData<PostModel>

    /**
     * retrieves a single post from the network and stores it locally
     * @param subredditName name of subreddit that the post was made in
     * @param postFullName "full name" of the subreddit
     */
    suspend fun retrievePost(
        subredditName: String,
        postFullName: String,
        postSource: PostSource,
        errorHandler : (error : RelicRequestError) -> Unit
    )

    /**
     * clears all current posts for this subreddit and retrieves new ones based on the sorting
     * method specified
     * @param sortType
     * @param sortScope
     */
    @Throws(RelicRequestError::class)
    suspend fun retrieveSortedPosts(
        postSource: PostSource,
        sortType: SortType,
        sortScope: SortScope
    )

    /**
     * //TODO tentative -> should expose or not
     * need to decide whether the viewModel should handle this or not
     * @param postSource
     */
    suspend fun clearAllPostsFromSource(postSource: PostSource)

    enum class SortType {
        DEFAULT, BEST, CONTROVERSIAL, HOT, NEW, RISING, TOP
    }

    enum class SortScope {
        NONE, HOUR, DAY, WEEK, MONTH, YEAR, ALL
    }

    /**
     * Used to represent how a post is being accessed
     * Eg. Accessing a post from the Frontpage uses the "Frontpage" source
     */
    sealed class PostSource : Parcelable {
        @Parcelize
        object Frontpage : PostSource()

        @Parcelize
        object All : PostSource()

        @Parcelize
        object Popular : PostSource()

        @Parcelize
        data class Subreddit(
            val subredditName : String
        ) : PostSource()

        @Parcelize
        data class User(
            val username : String,
            val retrievalOption: RetrievalOption
        ) : PostSource()
    }

    enum class RetrievalOption {
        Submitted, Comments,
        // these should only be available for the current user
        Saved, Upvoted, Downvoted, Gilded, Hidden
    }
}
