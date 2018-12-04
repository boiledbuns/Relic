package com.relic.data

import android.arch.lifecycle.LiveData

import com.relic.data.gateway.PostGateway
import com.relic.data.gateway.SubGateway
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.data.models.PostModel

interface PostRepository {
    /**
     * Exposes gateway for vm to interact with objects stored on the server
     * @return gateway object for exponsing post actions
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
    fun retrieveMorePosts(postSource: PostSource, listingAfter: String)

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
     * @param postFullName "full name" of the subreddit"
     */
    fun retrievePost(subredditName: String, postFullName: String)

    /**
     * clears all current posts for this subreddit and retrieves new ones based on the sorting
     * method specified
     * @param postSource
     * @param sortByCode
     */
    fun retrieveSortedPosts(postSource: PostSource, sortByCode: Int, sortScopeCode: Int)

    fun retrieveSortedPosts(postSource: PostSource, sortByCode: Int)

    /**
     * //TODO tentative -> should expose or not
     * need to decide whether the Viewmodel should handle this or not
     * @param postSource
     */
    fun clearAllSubPosts(postSource: PostSource)

    companion object {
        const val SORT_DEFAULT = 0
        const val SORT_BEST = 1
        const val SORT_CONTROVERSIAL = 2
        const val SORT_HOT = 3
        const val SORT_NEW = 4
        const val SORT_RISING = 5
        const val SORT_TOP = 6

        const val SCOPE_NONE = 0
        const val SCOPE_HOUR = 1
        const val SCOPE_DAY = 2
        const val SCOPE_WEEK = 3
        const val SCOPE_MONTH = 4
        const val SCOPE_YEAR = 5
        const val SCOPE_ALL = 6
    }

    sealed class PostSource {
        class Frontpage : PostSource()
        class All : PostSource()
        class Popular : PostSource()
        data class Subreddit(
            val subredditName : String
        ) : PostSource()
    }

}
