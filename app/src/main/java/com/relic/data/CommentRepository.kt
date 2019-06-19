package com.relic.data

import android.arch.lifecycle.LiveData
import com.relic.api.response.Listing

import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel

interface CommentRepository {

    /**
     * Exposes the comments as a liveData list
     * @param postFullName full name of a post
     * @param displayNRows if greater than 0, displays top x comments loaded
     */
    fun getComments(postFullName: String, displayNRows: Int = 0): LiveData<List<CommentModel>>

    fun getComments(retrievalOption: RetrievalOption): LiveData<List<CommentModel>>

    /**
     * retrieves comments for a post from the network and stores them locally
     * @param subName display name of a subreddit
     * @param postFullName id of a post
     * @param refresh whether to refresh comments or get next
     */
    suspend fun retrieveComments(subName: String, postFullName: String, refresh : Boolean) : CommentsAndPostData

    suspend fun retrieveCommentChildren(moreChildrenComment: CommentModel)

    /**
     * clears all locally stored comments
     * @param postFullName full name of the post to clear the comments for
     */
    suspend fun clearAllCommentsFromSource(postFullName: String)

    fun getReplies(parentId : String) : LiveData<List<CommentModel>>

    /**
     * @param parent full name of the parent
     */
    suspend fun postComment(parent: String, text : String)
}

data class CommentsAndPostData(
    val post : PostModel,
    val comments : List<CommentModel>
)