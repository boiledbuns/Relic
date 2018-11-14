package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.volley.Response

import com.google.gson.GsonBuilder
import com.relic.R
import com.relic.network.NetworkRequestManager
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.data.models.CommentModel
import com.relic.network.request.RelicOAuthRequest
import com.shopify.livedataktx.observe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class CommentRepositoryImpl(
        private val viewContext: Context,
        private val requestManager: NetworkRequestManager,
        private val listingRepo: ListingRepository
) : CommentRepository {

    private val ENDPOINT = "https://oauth.reddit.com/"
    private val userAgent = "android:com.relic.Relic (by /u/boiledbuns)"
    private val TAG = "COMMENT_REPO"

    private val appDB = ApplicationDB.getDatabase(viewContext)

    private val authToken: String?

    private val jsonParser = JSONParser()
    private val gson = GsonBuilder().create()
    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.CANADA)

    init {
        // TODO convert this to a authenticator method
        // retrieve the auth token shared preferences
        val authKey = viewContext.resources.getString(R.string.AUTH_PREF)
        val tokenKey = viewContext.resources.getString(R.string.TOKEN_KEY)
        authToken = viewContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
                .getString(tokenKey, "DEFAULT")
    }

    /**
     * Exposes the list of comments as livedata
     * @param postFullName fullname of the post to retrieve comments for
     * @return list of comments as livedata
     */
    override fun getComments(postFullName : String): LiveData<List<CommentModel>> {
        return appDB.commentDAO.getChildren(removeTypePrefix(postFullName))
    }

    override fun retrieveComments(subName: String, postId: String, refresh : Boolean) {
        if (refresh) {
            GlobalScope.launch { createRetrieveCommentsRequest(subName, postId) }
        }
        else {
            // TODO convert this from async to promise based
            listingRepo.getAfter(postId).observe {
                GlobalScope.launch { createRetrieveCommentsRequest(subName, postId, it) }
            }.removeObserver()
        }
    }

    private suspend fun createRetrieveCommentsRequest(subName: String, postFullName: String, after : String? = null) : String? {
        var ending = "r/$subName/comments/${postFullName.substring(3)}?count=20"
        after?.let { ending += "&after=$it" }

        var responseReturn : String? = null
        runBlocking {
            requestManager.processRequest(RelicOAuthRequest(
                    RelicOAuthRequest.GET,
                    ENDPOINT + ending,
                    Response.Listener { response ->
                        try {
                            responseReturn = response
                            GlobalScope.launch { parseCommentRequestResponse(postFullName, response) }
                        } catch (e: Exception) {
                            Log.d(TAG, "Error parsing JSON return " + e.message)
                        }
                    },
                    Response.ErrorListener{  error -> Log.d(TAG, "Error with request : " + error.message) },
                    authToken!!
            ))
        }
        return responseReturn
    }

    private suspend fun parseCommentRequestResponse(postFullName: String, response: String) {
        // the comment data is nested as the first element within an array
        val requestData = jsonParser.parse(response) as JSONArray
        val parentId = removeTypePrefix(postFullName)
        parseComments(parentId, requestData[1] as JSONObject)
    }

    /**
     * Parse the response from the api and store the comments in the room db
     * @param response json string response
     * @param parentId fullname of post used as a key for the "after" value
     * @throws ParseException potential issue with parsing of json structure
     */
    @Throws(ParseException::class)
    private suspend fun parseComments(parentId: String, response: JSONObject) {
        val commentsData = (response["data"] as JSONObject)
        val listing = ListingEntity(parentId, commentsData["after"]?.run { this as String })

        // get the list of children (comments) associated with the post
        val commentChildren = commentsData["children"] as JSONArray
        val commentEntities = ArrayList<CommentEntity>()

        commentChildren.forEach {
            val commentPOJO = (it as JSONObject)["data"] as JSONObject
            commentEntities.add(gson.fromJson(commentPOJO.toString(), CommentEntity::class.java).apply {
                Log.d(TAG, "parent id = ${this.parent_id} current id = ${commentPOJO["id"]?.toString()}")

                commentPOJO["replies"]?.let { childJson ->
                    if (childJson.toString().isNotEmpty()) {
                        // start another coroutine to parse the children of this comment
                        coroutineScope { parseComments(id, childJson as JSONObject) }

                        // set reply count for this object
                        val childJsonData = (childJson as JSONObject)["data"] as JSONObject
                        replyCount = (childJsonData["children"] as JSONArray).size
                    }
                }

                parent_id = removeTypePrefix(parent_id)

                userUpvoted = commentPOJO["likes"]?.run {
                    if (this as Boolean) 1 else -1
                } ?: 0

                commentPOJO["created"]?.apply { created = formatDate(this as Double) }

                // have to do this because reddit has a decided this can be boolean or string
                try {
                     editedDate = formatDate(commentPOJO["edited"] as Double)
                } catch (e : Exception) { }
            })
        }

        if (commentEntities.size > 0) {
            appDB.commentDAO.insertComments(commentEntities)
            appDB.listingDAO.insertListing(listing)
        }
    }

    private fun formatDate(epochTime : Double) : String? {
        var created: String?

        // add year to stamp if the post year doesn't match the current one
        Date(epochTime.toLong() * 1000).also { commentCreated ->
            created = if (Date().year != commentCreated.year) {
                commentCreated.year.toString() + " " + formatter.format(
                        commentCreated
                )
            } else {
                formatter.format(commentCreated)
            }
        }

        return created
    }

    override fun clearComments(postFullName: String) {
        ClearCommentsTask().execute(appDB, removeTypePrefix(postFullName))
    }

    private class ClearCommentsTask : AsyncTask<Any, Int, Unit>() {
        override fun doInBackground(vararg objects: Any) {
            val appDB = objects[0] as ApplicationDB
            val postId = objects[1] as String
            appDB.commentDAO.deletePostComments(postId)
        }
    }

    override fun getReplies(parentId: String): LiveData<List<CommentModel>> {
        return appDB.commentDAO.getChildren(parentId)
    }

    companion object {
        private const val postPrefix = "t3"
        private const val commentPrefix = "t1"
        private const val notEdited = "false"

        /**
         * removes the type associated with the comment, leaving only its id
         */
        fun removeTypePrefix(fullName : String) : String = fullName.removeRange(0, 3)

        fun hasBeenEdited(editedString : String) : Boolean = (editedString != notEdited)
    }
}
