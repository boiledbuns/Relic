package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.text.Html
import android.util.Log
import com.android.volley.VolleyError

import com.google.gson.GsonBuilder
import com.relic.R
import com.relic.data.deserializer.CommentDeserializer
import com.relic.data.deserializer.ParsedCommentData
import com.relic.network.NetworkRequestManager
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.data.models.CommentModel
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.*

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import kotlin.math.pow

class CommentRepositoryImpl(
        private val viewContext: Context,
        private val requestManager: NetworkRequestManager,
        private val listingRepo: ListingRepository
) : CommentRepository {

    companion object {
        private const val ENDPOINT = "https://oauth.reddit.com/"
        private const val NON_OAUTH_ENDPOINT = "https://www.reddit.com/"

        private const val userAgent = "android:com.relic.Relic (by /u/boiledbuns)"
        private const val TAG = "COMMENT_REPO"

        const val TYPE_COMMENT = "t1"
    }

    private val appDB = ApplicationDB.getDatabase(viewContext)

    private val authToken: String?

    private val jsonParser = JSONParser()

    init {
        // TODO convert this to a authenticator method
        // retrieve the auth token shared preferences
        val authKey = viewContext.resources.getString(R.string.AUTH_PREF)
        val tokenKey = viewContext.resources.getString(R.string.TOKEN_KEY)
        authToken = viewContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
                .getString(tokenKey, "DEFAULT")
    }

    // region interface

    override fun getComments(postFullName : String, displayNRows: Int): LiveData<List<CommentModel>> {
        val postFullname = CommentDeserializer.removeTypePrefix(postFullName)
        return when {
            (displayNRows > 0) -> {
                appDB.commentDAO.getChildrenByLevel(postFullname, displayNRows)
            }
            else -> {
                appDB.commentDAO.getAllComments(postFullname)
            }
        }
    }

    override suspend fun retrieveComments(
        subName: String,
        postFullName: String,
        refresh : Boolean
    ) {
        val postName = CommentDeserializer.removeTypePrefix(postFullName)
        var url = "${ENDPOINT}r/$subName/comments/$postName?count=20"

        if (refresh) {
            val after = withContext(Dispatchers.Default) { listingRepo.getAfterString(postFullName) }
            url += "&after=$after"
        }

        try {
            val response = requestManager.processRequest(RelicOAuthRequest.GET, url, authToken!!)
            val parsedData = parseCommentRequestResponse(
                postFullName = postFullName,
                response = response
            )

            coroutineScope {
                launch (Dispatchers.IO) {
                    insertComments(parsedData.commentList, parsedData.listingEntity)
                }
            }
        }

        catch (e : Exception) {
            when (e) {
                is VolleyError -> Log.d(TAG, "Error with request : at $url \n ${e.networkResponse}")
                else -> Log.d(TAG, "Error parsing JSON return " + e.message)
            }
        }
    }

    override suspend fun retrieveCommentChildren(commentModel: CommentModel) {
//        val url = "${ENDPOINT}api/morechildren?api_type=json&link_id=${commentModel.fullName}&id=${commentModel.replyLink}"
        val url = "${ENDPOINT}api/morechildren?api_type=json&link_id=${commentModel.fullName}&children=${commentModel.replyLink}"

        try {
            val response = requestManager.processRequest(RelicOAuthRequest.GET, url, authToken!!)
            val parsedData = parseCommentRequestResponse(
                postFullName = commentModel.fullName,
                response = response
            )

            coroutineScope {
                launch (Dispatchers.IO) { insertComments(parsedData.commentList, parsedData.listingEntity) }
            }
        } catch (e : Exception) {
            when (e) {
                is VolleyError -> Log.d(TAG, "Error with request : at $url \n ${e.localizedMessage}")
                else -> Log.d(TAG, "Error parsing JSON return " + e.message)
            }
        }
    }

    override fun clearComments(postFullName: String) {
        ClearCommentsTask().execute(appDB, CommentDeserializer.removeTypePrefix(postFullName))
    }

    override fun getReplies(parentId: String): LiveData<List<CommentModel>> {
        return appDB.commentDAO.getAllComments(parentId)
    }

    // endregion interface

    // region helper

    private suspend fun parseCommentRequestResponse(
        postFullName: String,
        response: String
    ) : ParsedCommentData {
        // the comment data is nested as the first element within an array
        val requestData = jsonParser.parse(response) as JSONArray
        val parentPostId = CommentDeserializer.removeTypePrefix(postFullName)

        return CommentDeserializer.parseComments(parentPostId, requestData[1] as JSONObject)
    }

    // endregion helper

    // region async tasks

    private fun insertComments(comments : List<CommentEntity>, listing : ListingEntity) {
        appDB.commentDAO.insertComments(comments)
        appDB.listingDAO.insertListing(listing)
    }

    private class ClearCommentsTask : AsyncTask<Any, Int, Unit>() {
        override fun doInBackground(vararg objects: Any) {
            val appDB = objects[0] as ApplicationDB
            val postId = objects[1] as String
            appDB.commentDAO.deletePostComments(postId)
        }
    }

    // endregion async tasks
}