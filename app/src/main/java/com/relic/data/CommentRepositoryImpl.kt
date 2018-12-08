package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.text.Html
import android.util.Log
import com.android.volley.Response

import com.google.gson.GsonBuilder
import com.relic.R
import com.relic.network.NetworkRequestManager
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.data.models.CommentModel
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    companion object {
        private const val ENDPOINT = "https://oauth.reddit.com/"
        private const val NON_OAUTH_ENDPOINT = "https://www.reddit.com/"

        private const val userAgent = "android:com.relic.Relic (by /u/boiledbuns)"
        private const val TAG = "COMMENT_REPO"

        // removes the type associated with the comment, leaving only its id
        fun removeTypePrefix(fullName : String) : String = fullName.removeRange(0, 3)
    }

    private val appDB = ApplicationDB.getDatabase(viewContext)

    private val authToken: String?

    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.CANADA)
    private val currentYear = Date().year

    private val jsonParser = JSONParser()
    private val gson = GsonBuilder().create()

    init {
        // TODO convert this to a authenticator method
        // retrieve the auth token shared preferences
        val authKey = viewContext.resources.getString(R.string.AUTH_PREF)
        val tokenKey = viewContext.resources.getString(R.string.TOKEN_KEY)
        authToken = viewContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
                .getString(tokenKey, "DEFAULT")
    }

    // region interface

    override fun getComments(postFullName : String): LiveData<List<CommentModel>> {
        return appDB.commentDAO.getChildren(removeTypePrefix(postFullName))
    }

    override fun retrieveComments(subName: String, postFullName: String, refresh : Boolean) {
        if (refresh) {
            GlobalScope.launch (Dispatchers.IO) {
                async { createRetrieveCommentsRequest(subName, postFullName) }
            }
        }
        else {
//            val removableObserver = listingRepo.getAfter(postFullName)
//            removableObserver.observeForever(object : Observer<String?> {
//                override fun onChanged(afterValue: String?) {
//                    GlobalScope.launch { createRetrieveCommentsRequest(subName, postFullName, afterValue) }
//                    removableObserver.removeObserver(this)
//                }
//            })
            GlobalScope.launch {
                var after : String? = null
                async { after = listingRepo.getAfterString(postFullName) ?: "empty_after"}.await()
                after?.let { createRetrieveCommentsRequest(subName, postFullName, after) }

                Log.d(TAG, "async check $after")
            }

        }
    }

    override fun retrieveCommentChildren(commentModel: CommentModel) {
        GlobalScope.launch (Dispatchers.IO) {
            val ending = "api/morechildren?api_type=json&link_id=${commentModel.fullName}&id=${commentModel.replyLink}"

            requestManager.processRequest(RelicOAuthRequest(
                RelicOAuthRequest.GET,
                NON_OAUTH_ENDPOINT + ending,
                Response.Listener { response ->
                    try {
                        Log.d(TAG, "More replies $response")
                    } catch (e: Exception) {
                        Log.d(TAG, "Error parsing JSON return ${e.message}")
                    }
                },
                Response.ErrorListener{  error -> Log.d(TAG, "Error with request : " + error.message) },
                authToken!!
            ))

        }
    }

    override fun clearComments(postFullName: String) {
        ClearCommentsTask().execute(appDB, removeTypePrefix(postFullName))
    }

    override fun getReplies(parentId: String): LiveData<List<CommentModel>> {
        return appDB.commentDAO.getChildren(parentId)
    }

    // endregion interface

    // region helper

    private fun createRetrieveCommentsRequest(
        subName: String,
        postFullName: String,
        after : String? = null
    ) : String? {
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
                        // TODO convert to promise based
                        parseCommentRequestResponse(postFullName, response)
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

    private fun parseCommentRequestResponse(postFullName: String, response: String) {
        // the comment data is nested as the first element within an array
        val requestData = jsonParser.parse(response) as JSONArray
        val parentId = removeTypePrefix(postFullName)
        GlobalScope.launch { parseComments(parentId, requestData[1] as JSONObject) }
    }

    /**
     * Parse the response from the api and store the comments in the room db
     * @param response json string response
     * @param parentId full name of post used as a key for the "after" value
     * @return : size of the list to the child list
     * @throws ParseException potential issue with parsing of json structure
     */
    @Throws(ParseException::class)
    private suspend fun parseComments(parentId: String, response: JSONObject) : Int {
        val commentsData = (response["data"] as JSONObject)
        val listing = ListingEntity(parentId, commentsData["after"]?.run { this as String })

        // get the list of children (comments) associated with the post
        val commentChildren = commentsData["children"] as JSONArray
        val deferredList = ArrayList<Deferred<CommentEntity>>()

        coroutineScope {
            async {
                commentChildren.forEach { commentChild ->
                    deferredList.add(
                        async { unmarshallComment(commentChild as JSONObject) }
                    )
                }

                launch (Dispatchers.IO) {
                    InsertPostsTask().execute(appDB, deferredList.awaitAll(), listing)
                }
            }
        }

        return deferredList.awaitAll().size
    }

    // TODO find a better way to unmarshall these objects and clean this up
    // won't be cleaned for a while because still decided how to format data and what is needed
    private suspend fun unmarshallComment(commentChild : JSONObject) : CommentEntity {
        val commentPOJO = commentChild["data"] as JSONObject

        return gson.fromJson(commentPOJO.toString(), CommentEntity::class.java).apply {
            commentPOJO["replies"]?.let { childJson ->
                // try to parse the child json as nested replies
                if (childJson.toString().isNotEmpty()) {
                    // start another coroutine to parse the children of this comment
                    coroutineScope {
                        replyCount = async { parseComments(id, childJson as JSONObject) }.await()
                    }
                    // set reply count for this object
                    val childJsonData = (childJson as JSONObject)["data"] as JSONObject
                    // replyCount = (childJsonData["children"] as JSONArray).size
                }
            }

            parent_id = removeTypePrefix(parent_id)
            Log.d(TAG, "parent id = ${this.parent_id} current id = ${this.id}")

            userUpvoted = commentPOJO["likes"]?.run {
                if (this as Boolean) 1 else -1
            } ?: 0

            author_flair_text?.let {
                author_flair_text = Html.fromHtml(author_flair_text).toString()
            }
            commentPOJO["created"]?.apply { created = formatDate(this as Double) }

            // have to do this because Reddit has a decided this can be boolean or string
            try {
                editedDate = formatDate(commentPOJO["edited"] as Double)
            } catch (e: Exception) { }
        }
    }

    private fun formatDate(epochTime : Double) : String? {
        val commentCreated = Date(epochTime.toLong() * 1000)

        return if (currentYear != commentCreated.year) {
            // add year if the comment wasn't made in the current year
            "${commentCreated.year} ${formatter.format(commentCreated)}"
        } else {
            formatter.format(commentCreated)
        }
    }

    // endregion helper

    // region async tasks

    private class InsertPostsTask : AsyncTask<Any, Int, Unit>() {
        override fun doInBackground(vararg objects: Any) {
            val appDB = objects[0] as ApplicationDB
            val comments = objects[1] as List<CommentEntity>
            val listing = objects[2] as ListingEntity

            appDB.commentDAO.insertComments(comments)
            appDB.listingDAO.insertListing(listing)
        }
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
