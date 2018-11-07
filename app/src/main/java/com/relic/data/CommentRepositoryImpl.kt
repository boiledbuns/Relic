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
        private val requestManager: NetworkRequestManager
) : CommentRepository {
    private val ENDPOINT = "https://oauth.reddit.com/"
    private val userAgent = "android:com.relic.Relic (by /u/boiledbuns)"
    private val TAG = "COMMENT_REPO"

    private val appDB: ApplicationDB
    private val JSONParser: JSONParser

    private val authToken: String?
    private val gson = GsonBuilder().create()

    init {
        //TODO convert VolleyQueue into a singleton
        appDB = ApplicationDB.getDatabase(viewContext)
        JSONParser = JSONParser()

        // TODO convert this to a authenticator method
        // retrieve the auth token shared preferences
        val authKey = viewContext.resources.getString(R.string.AUTH_PREF)
        val tokenKey = viewContext.resources.getString(R.string.TOKEN_KEY)
        authToken = viewContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
                .getString(tokenKey, "DEFAULT")
    }

    /**
     * Exposes the list of comments as livedata
     * @param postFullname fullname of the post to retrieve comments for
     * @return list of comments as livedata
     */
    override fun getComments(postFullname: String): LiveData<List<CommentModel>> {
        return appDB.commentDAO.getComments(postFullname)
    }

    override fun retrieveComments(subName: String, postFullName: String, after: String?) {
        var ending = "r/" + subName + "/comments/" + postFullName.substring(3) + "?count=20"
        Log.d(TAG, ENDPOINT + ending)
        if (after != null) {
            ending += "&after=$after"
        }
        requestManager.processRequest(
                RelicOAuthRequest(
                        RelicOAuthRequest.GET,
                        ENDPOINT + ending,
                        Response.Listener { response ->
                            Log.d(TAG, response)
                            try {
                                parseComments(postFullName, response)
                            } catch (e: Exception) {
                                Log.d(TAG, "Error parsing JSON return " + e.message)
                            }
                        },
                        Response.ErrorListener{  error -> Log.d(TAG, "Error with request : " + error.message) },
                        authToken!!
                )
        )
    }

    override fun clearComments(postFullname: String) {
        ClearCommentsTask().execute(appDB, postFullname)
    }

    private class ClearCommentsTask : AsyncTask<Any, Int, Int>() {
        override fun doInBackground(vararg objects: Any): Int? {
            val appDB = objects[0] as ApplicationDB
            val postFullname = objects[1] as String

            // delete the locally stored post comment data using the comment dao
            appDB.commentDAO.deletePostComments(postFullname)
            return null
        }
    }

    /**
     * Parse the response from the api and store the comments in the room db
     * @param response json string response
     * @param postFullName fullname of post used as a key for the "after" value
     * @throws ParseException potential issue with parsing of json structure
     */
    @Throws(ParseException::class)
    private fun parseComments(postFullName: String, response: String) {
        val responseElement = (JSONParser.parse(response) as JSONArray)[1] as JSONObject
        val commentsData = (responseElement["data"] as JSONObject)
        val listing = ListingEntity(postFullName, commentsData["after"]?.run { this as String })
//        Log.d(TAG, "after = " + commentsData["after"]?.run { this as String })

        // get the list of children (comments) associated with the post
        val commentChildren = commentsData["children"] as JSONArray
        val commentEntities = ArrayList<CommentEntity>()

        // initialize the date formatter and date for now
        //  TODO optimize this
        val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.CANADA)
        val current = Date()

        commentChildren.forEach {
            val commentPOJO = (it as JSONObject)["data"] as JSONObject
            commentEntities.add(gson.fromJson(commentPOJO.toString(), CommentEntity::class.java).apply {

                id = commentPOJO["id"] as String

                userUpvoted = commentPOJO["likes"]?.run {
                    if (this as Boolean) 1 else -1
                } ?: 0

                // add year to stamp if the post year doesn't match the current one
                Date((commentPOJO["created"] as Double).toLong() * 1000).also { commentCreated ->
                    created = if (current.year != commentCreated.year) {
                        commentCreated.year.toString() + " " + formatter.format(commentCreated)
                    }
                    else {
                        formatter.format(commentCreated)
                    }
                }
            })
        }

        InsertCommentsTask(appDB, commentEntities, listing).execute()
    }

    private class InsertCommentsTask internal constructor(
            internal var db: ApplicationDB,
            internal var comments: List<CommentEntity>,
            internal var listing: ListingEntity
    ) : AsyncTask<String, Int, Int>() {

        override fun doInBackground(vararg strings: String): Int? {
            db.commentDAO.insertComments(comments)
            db.listingDAO.insertListing(listing)
            return null
        }
    }
}
