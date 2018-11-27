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
     * exposes livedata list of posts for a given subreddit
     * @param subredditName name of the subreddit to retrieve the posts for
     * @return list of posts from this subreddit as livedata (empty if none)
     */
    fun getPosts(subredditName: String): LiveData<List<PostModel>>

    /**
     * retrieves more posts from the network and store them locally
     * @param subredditName valid subreddit name
     * @param postingAfter null ? refresh : "after" value for the next listing
     */
    fun retrieveMorePosts(subredditName: String, postingAfter: String)

    /**
     *
     * @param callback
     * @param subName
     */
    fun getNextPostingVal(callback: RetrieveNextListingCallback, subName: String)

    /**
     * exposes a single post model as livedata
     * @param postFullName a valid "full name" for a post
     * @return a model of the post wrapped in Livedata
     */
    fun getPost(postFullName: String): LiveData<PostModel>

    // TODO convert this to return livedata and get rid of get post
    /**
     * retrieves a single post from the network and stores it locally
     * @param subredditName name of subreddit that the post was made in
     * @param postFullName "full name" of the subreddit"
     */
    fun retrievePost(subredditName: String, postFullName: String)

    /**
     * clears all current posts for this subreddit and retrieves new ones based on the sorting
     * method specified
     * @param subredditName
     * @param sortByCode
     */
    fun retrieveSortedPosts(subredditName: String, sortByCode: Int, sortScopeCode: Int)

    fun retrieveSortedPosts(subredditName: String, sortByCode: Int)

    /**
     * //TODO tentative -> should expose or not
     * need to decide whether the Viewmodel should handle this or not
     * @param subredditName
     */
    fun clearAllSubPosts(subredditName: String)

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

}
