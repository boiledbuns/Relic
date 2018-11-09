package com.relic.data

import android.arch.lifecycle.LiveData

import com.relic.data.models.CommentModel
import com.relic.data.models.PostModel

interface CommentRepository {

    /**
     * Exposes the comments as a livedata list
     * @param postFullname fullname of a post
     */
    fun getComments(postFullname: String): LiveData<List<CommentModel>>

    /**
     * retrieves comments for a post from the network and stores them locally
     * @param subName display name of a subreddit
     * @param postFullname full name of the post
     * @param refresh whether to refresh comments or get next
     */
    fun retrieveComments(subName: String, postFullname: String, refresh : Boolean)

    /**
     * clears all locally stored comments
     * @param postFullname full name of the post to clear the comments for
     */
    fun clearComments(postFullname: String)
}
