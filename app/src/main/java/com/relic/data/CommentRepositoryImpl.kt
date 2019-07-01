package com.relic.data

import android.arch.lifecycle.LiveData
import com.relic.data.deserializer.Contract
import com.relic.data.repository.RepoConstants
import com.relic.domain.models.CommentModel
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.relic.persistence.ApplicationDB
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CommentRepositoryImpl @Inject constructor(
    private val requestManager: NetworkRequestManager,
    private val appDB : ApplicationDB,
    private val listingRepo: ListingRepository,
    private val commentDeserializer: Contract.CommentDeserializer
) : CommentRepository {
    private val commentDao = appDB.commentDAO

    override suspend fun getComments(postFullName : String, displayNRows: Int): List<CommentModel> {
        return when {
            (displayNRows > 0) -> {
                withContext(Dispatchers.IO) { commentDao.getChildrenByLevel(postFullName, displayNRows) }
            }
            else -> {
                withContext(Dispatchers.IO) { commentDao.getAllComments(postFullName) }
            }
        }
    }

    override fun getComments(retrievalOption: RetrievalOption): LiveData<List<CommentModel>> {
        return appDB.userPostingDao.getUserComments(retrievalOption.name)
    }

    override suspend fun retrieveComments(subName: String, postFullName: String, refresh : Boolean) : CommentsAndPostData {
        val postName = commentDeserializer.removeTypePrefix(postFullName)
        var url = "${RepoConstants.ENDPOINT}r/$subName/comments/$postName?count=20"

        if (refresh) {
            val after = listingRepo.getAfter(PostSource.Post(postFullName))
            url += "&after=$after"
        }

        try {
            val response = requestManager.processRequest(RelicOAuthRequest.GET, url)
            Timber.d("$response")

            return commentDeserializer.parseCommentsAndPost(response)
        }
        catch (e : Exception) {
            throw DomainTransfer.handleException("retrieve comments", e) ?: e
        }
    }

    override suspend fun retrieveCommentChildren(postFullName: String, moreChildrenComment: CommentModel) : List<CommentModel> {
        val url = "${RepoConstants.ENDPOINT}api/morechildren"
        val idList = moreChildrenComment.more!!.toString().drop(1).dropLast(1)
        val postData = HashMap<String, String>().apply {
            put("api_type", "json")
            put("children", idList)
            put("limit_children", "false")
            put("link_id", postFullName)
            put("sort", "confidence")
        }

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.POST,
                url = url,
                data = postData
            )

            return commentDeserializer.parseMoreCommentsResponse(moreChildrenComment, response)
        } catch (e : Exception) {
            throw DomainTransfer.handleException("retrieve comment children", e) ?: e
        }
    }

    override suspend fun insertComments(comments : List<CommentModel>)= withContext(Dispatchers.IO) {
        commentDao.insertComments(comments)
    }

    override suspend fun deleteComments(postFullName: String) = withContext(Dispatchers.IO){
        commentDao.deletePostComments(postFullName)
    }

    override suspend fun postComment(parent: String, text: String) {
        val url = "${RepoConstants.ENDPOINT}api/comment"
        val data = HashMap<String, String>().apply {
            put("thing_id", parent)
            put("text", text)
        }

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.POST,
                url = url,
                data = data
            )

            Timber.d(response)
        } catch (e : Exception) {
            throw DomainTransfer.handleException("post comment", e) ?: e
        }
    }
}