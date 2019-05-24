package com.relic.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log

import com.relic.data.deserializer.CommentDeserializer
import com.relic.network.NetworkRequestManager
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.data.models.CommentModel
import com.relic.data.repository.RepoConstants
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.*

class CommentRepositoryImpl(
    private val viewContext: Context,
    private val requestManager: NetworkRequestManager,
    private val listingRepo: ListingRepository
) : CommentRepository {
    private val TAG = "COMMENT_REPO"

    private val appDB = ApplicationDB.getDatabase(viewContext)
    private val commentDao = appDB.commentDAO

    // region interface

    override fun getComments(postFullName : String, displayNRows: Int): LiveData<List<CommentModel>> {
        val postId = CommentDeserializer.removeTypePrefix(postFullName)
        return when {
            (displayNRows > 0) -> {
                commentDao.getChildrenByLevel(postId, displayNRows)
            }
            else -> {
                commentDao.getAllComments(postId)
            }
        }
    }

    override fun getComments(retrievalOption: PostRepository.RetrievalOption): LiveData<List<CommentModel>> {
        return when (retrievalOption) {
            PostRepository.RetrievalOption.Submitted -> MutableLiveData()
            PostRepository.RetrievalOption.Comments -> appDB.userPostingDao.getUserComments()
            PostRepository.RetrievalOption.Saved -> appDB.userPostingDao.getUserSavedComments()
            PostRepository.RetrievalOption.Upvoted -> appDB.userPostingDao.getUserUpvotedComments()
            PostRepository.RetrievalOption.Downvoted -> appDB.userPostingDao.getUserDownvotedComments()
            PostRepository.RetrievalOption.Gilded -> appDB.userPostingDao.getUserGildedComments()
            PostRepository.RetrievalOption.Hidden -> appDB.userPostingDao.getUserHiddenComments()
        }
    }

    override suspend fun retrieveComments(subName: String, postFullName: String, refresh : Boolean) {
        val postName = CommentDeserializer.removeTypePrefix(postFullName)
        var url = "${RepoConstants.ENDPOINT}r/$subName/comments/$postName?count=20"

        if (refresh) {
            val after = listingRepo.getAfterString(postFullName)
            url += "&after=$after"
        }

        try {
            val response = requestManager.processRequest(RelicOAuthRequest.GET, url)
            Log.d(TAG, "$response")

            val parsedData = CommentDeserializer.parseCommentsResponse(
                postFullName = postFullName,
                response = response
            )

            insertComments(parsedData.commentList, parsedData.listingEntity)
        }
        catch (e : Exception) {
            throw DomainTransfer.handleException("retrieve comments", e) ?: e
        }
    }

    override suspend fun retrieveCommentChildren(moreChildrenComment: CommentModel) {
        val url = "${RepoConstants.ENDPOINT}api/morechildren"

        val removedQuotations = moreChildrenComment.body.replace("\"", "")
        val idList = removedQuotations.subSequence(1, removedQuotations.length - 1)

        val postData = HashMap<String, String>().apply {
            put("api_type", "json")
            put("children", idList.toString())
            put("limit_children", "false")
            put("link_id", "t3_" + moreChildrenComment.parentPostId)
            put("sort", "confidence")
        }

        val response = requestManager.processRequest(
            method = RelicOAuthRequest.POST,
            url = url,
            data = postData
        )

        try {
            val commentEntities = CommentDeserializer.parseMoreCommentsResponse(moreChildrenComment, response)

            withContext (Dispatchers.IO) {
                commentDao.deleteComment(moreChildrenComment.fullName)
                commentDao.insertComments(commentEntities)
            }
        } catch (e : Exception) {
            throw DomainTransfer.handleException("retrieve comment children", e) ?: e
        }
    }

    override suspend fun clearAllCommentsFromSource(postFullName: String) {
        withContext(Dispatchers.IO){
            commentDao.deletePostComments(CommentDeserializer.removeTypePrefix(postFullName))
        }
    }

    override fun getReplies(parentId: String): LiveData<List<CommentModel>> {
        return commentDao.getAllComments(parentId)
    }

    // endregion interface


    /**
     * only use this function to insert comments to ensure they're inserted with an associated
     * listing
     */
    private suspend fun insertComments(comments : List<CommentEntity>, listing : ListingEntity) {
        withContext(Dispatchers.IO) {
            commentDao.insertComments(comments)
            appDB.listingDAO.insertListing(listing)
        }
    }
    
}