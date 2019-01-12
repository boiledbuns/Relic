package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.text.Html
import android.util.Log
import com.android.volley.VolleyError

import com.google.gson.GsonBuilder
import com.relic.R
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

    override fun getComments(postFullName : String, displayNRows: Int): LiveData<List<CommentModel>> {
        val postFullname = removeTypePrefix(postFullName)
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
        val postName = removeTypePrefix(postFullName)
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
        ClearCommentsTask().execute(appDB, removeTypePrefix(postFullName))
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
        val parentPostId = removeTypePrefix(postFullName)

        return parseComments(parentPostId, requestData[1] as JSONObject)
    }

    /**
     * Parse the response from the api and store the comments in the room db
     * @param response json string response
     * @param postFullName full name of post used as a key for the "after" value
     * @param parentDepth depth of the parent. Since posts start with a depth of 0, -1 is the
     * depth of the parent when calling from outside a recursive call
     * @param parentPosition positional value of the parent
     * @return : Parsed comment
     * @throws ParseException potential issue with parsing of json structure
     */
    @Throws(ParseException::class)
    private suspend fun parseComments(
        postFullName: String,
        response: JSONObject,
        parentDepth : Int = -1,
        parentPosition : Float = 0f
    ) : ParsedCommentData {
        val commentsData = (response["data"] as JSONObject)
        val listing = ListingEntity(postFullName, commentsData["after"]?.run { this as String })

        // get the list of children (comments) associated with the post
        val commentChildren = commentsData["children"] as JSONArray
        val commentList = ArrayList<CommentEntity>()

        // used for calculating the position of a comment
        val scale = 10f.pow(-(parentDepth + 1))
        var childCount = 1

        coroutineScope {
            commentChildren.forEach { commentChild ->
                val position = parentPosition + childCount*scale
                val commentJson = commentChild as JSONObject
                val childKind = commentJson["kind"] as String?

                if (childKind == "more") {
                    // means there is a "more object"
                    val deferredMore = async {
                        val moreData = (commentJson["data"] as JSONObject)
                        unmarshallMore(moreData, postFullName, position)
                    }
                    commentList.add(deferredMore.await())
                } else {
                    val deferredCommentList = async {
                        unmarshallComment(commentJson, postFullName, position)
                    }
                    commentList.addAll(deferredCommentList.await())
                }

                childCount ++
            }
        }

        return ParsedCommentData(listing, commentList, commentChildren.size)
    }

    // TODO refactor and move the method into a comment entity method
    // TODO find a better way to unmarshall these objects and clean this up
    // won't be cleaned for a while because still decided how to format data and what is needed
    private suspend fun unmarshallComment(
        commentChild : JSONObject,
        postFullName : String,
        commentPosition : Float
    ) : List<CommentEntity> {
        val commentPOJO = commentChild["data"] as JSONObject
        val commentList = ArrayList<CommentEntity>()

        coroutineScope {

            var deferredCommentData: Deferred<ParsedCommentData>? = null
            val commentEntity = gson.fromJson(commentPOJO.toString(), CommentEntity::class.java).apply {
                parentPostId = postFullName
                position = commentPosition

                commentPOJO["replies"]?.let { childJson ->
                    // try to parse the child json as nested replies
                    if (childJson.toString().isNotEmpty()) {
                        // parse the children of this comment
                        deferredCommentData = async {
                            parseComments(
                                postFullName = postFullName,
                                response = childJson as JSONObject,
                                parentDepth = depth,
                                parentPosition = commentPosition
                            )
                        }
                    }
                }

                // converts fields that have already been unmarshalled by gson
                parent_id = removeTypePrefix(parent_id)
                author_flair_text?.let {
                    author_flair_text = Html.fromHtml(author_flair_text).toString()
                }

                // converts fields from json not in explicitly unmarshalled by gson
                userUpvoted = commentPOJO["likes"]?.run {
                    if (this as Boolean) 1 else -1
                } ?: 0

                commentPOJO["created"]?.let { created = formatDate(it as Double) }

                // get the gildings
                (commentPOJO["gildings"] as JSONObject?)?.let { gilding ->
                    platinum = (gilding["gid_1"] as Long).toInt()
                    gold = (gilding["gid_2"] as Long).toInt()
                    silver = (gilding["gid_3"] as Long).toInt()
                }

                // have to do this because Reddit has a decided this can be boolean or string
                try {
                    editedDate = formatDate(commentPOJO["edited"] as Double)
                } catch (e: Exception) { }
            }

            deferredCommentData?.let {
                it.await().let { parsedData ->
                    commentEntity.replyCount = parsedData.replyCount
                    commentList.addAll(parsedData.commentList)
                }
            }

            commentList.add(commentEntity)
        }

        return commentList
    }

    private fun unmarshallMore(
        moreJsonObject : JSONObject,
        postFullName : String,
        commentPosition : Float
    ) : CommentEntity {
        return CommentEntity().apply {
            id = moreJsonObject["name"] as String
            parentPostId = postFullName
            parent_id = moreJsonObject["parent_id"] as String
            created = CommentEntity.MORE_CREATED
            position = commentPosition
            depth = (moreJsonObject["depth"] as Long).toInt()
            replyCount = (moreJsonObject["count"] as Long).toInt()

            val childrenLinks = moreJsonObject["children"] as JSONArray
            body_html = childrenLinks.toString()
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

private data class ParsedCommentData(
    val listingEntity : ListingEntity,
    val commentList : List<CommentEntity>,
    val replyCount : Int
)