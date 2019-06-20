package com.relic.data.deserializer

import com.google.gson.GsonBuilder
import com.relic.api.response.Listing
import com.relic.data.CommentsAndPostData
import com.relic.data.entities.ListingEntity
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList
import kotlin.math.pow

// TODO convert to object and add interface so this can be injected
@Singleton
class CommentDeserializerImpl @Inject constructor(
    private val moshi: Moshi
): Contract.CommentDeserializer {
    private val TAG = "COMMENT_DESERIALIZER"

    private val jsonParser: JSONParser = JSONParser()
    private val gson = GsonBuilder().create()
    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.CANADA)
    private val currentYear = Date().year

    private val commentAdapter = moshi.adapter(CommentModel::class.java)
    private val commentType = Types.newParameterizedType(Listing::class.java, CommentModel::class.java)
    private val commentListingAdapter = moshi.adapter<Listing<CommentModel>>(commentType)

    private val commentListType = Types.newParameterizedType(List::class.java, CommentModel::class.java)
    private val commentListAdapter = moshi.adapter<List<CommentModel>>(commentListType)

    private val postType = Types.newParameterizedType(Listing::class.java, ListingItem::class.java)
    private val postListingAdapter = moshi.adapter<Listing<PostModel>>(postType)

    // region interface methods

    override suspend fun parseCommentsResponse(
        postFullName: String,
        response: String
    ) : ParsedCommentData {
        // the comment data is nested as the second element within an array
        val requestData = jsonParser.parse(response) as JSONArray
        val parentPostId = removeTypePrefix(postFullName)

        return try {
            parseComments(parentPostId, requestData[1] as JSONObject)
        } catch (e : ParseException) {
            throw RelicParseException(response, e)
        }
    }

    override suspend fun parseCommentsAndPost(response : String) : CommentsAndPostData {
        return withContext(Dispatchers.Default) {
            try {
                // the comment data is nested as the second element within an array
                val parentChildList = jsonParser.parse(response) as JSONArray

                Timber.d("parse comments child ${parentChildList[1].toString()}")
                val postListing = postListingAdapter.fromJson(parentChildList[0].toString()) ?: throw RelicParseException(response)
                val commentListing= parseComments(parentChildList[1] as JSONObject)

                CommentsAndPostData(postListing.data.children!!.first(), commentListing)
            } catch (e : ParseException) {
                throw RelicParseException(response, e)
            }
        }
    }

    private suspend fun parseComments(listing : JSONObject) : MutableList<CommentModel> {
        // moshi can't handle this, so we have no choice but to parse the nested replies separate
        // from how individual comments are parsed.
        val replies = listing.unwrapListing().children()
        return replies.fold(ArrayList()) { accum, commentJson ->
            val comment = (commentJson as JSONObject).unwrapChild()
            val currentComment = commentAdapter.fromJson(commentJson.toString())!!

//            val kind = commentJson["kind"] as String
//            if (kind == "more") {
//                more = comment["more"] as JSONObject.
//            }

            // apparently this could be a string as well
            var repliesListingJson : JSONObject? = null
            try {
                repliesListingJson = comment["replies"] as JSONObject?
            } catch (e : ClassCastException) { }

            // recursive call to parse replies to this comment
            val children = if (repliesListingJson != null) parseComments(repliesListingJson) else null

            Timber.d("current comment $currentComment")
            Timber.d("replies ${children?.size} : $repliesListingJson")
            
            // accumulate comment and its replies
            accum.apply {
                add(currentComment)
                if (children != null) {
                    addAll(children)
                    currentComment.replyCount = children.size
                }
            }
        }
    }

    private fun JSONObject.unwrapListing() : JSONObject {
        return get("data") as JSONObject
    }

    private fun JSONObject.unwrapChild() : JSONObject {
        return get("data") as JSONObject
    }

    private fun JSONObject.children() : JSONArray {
        return get("children") as JSONArray
    }


    /**
     * Only use this method to parse the return from "morechildren" since it uses a different
     * format than the traditional method for retrieving comments
     */
    override suspend fun parseMoreCommentsResponse(
        moreChildrenComment: CommentModel,
        response: String
    ) : List<CommentModel> {
        val requestJson = (jsonParser.parse(response) as JSONObject)["json"] as JSONObject
        val requestComments = requestJson.unwrapListing()["things"] as JSONArray

        Timber.d( "load more scale ${moreChildrenComment.position}")

        try {
            return commentListAdapter.fromJson(requestComments.toString())!!
        } catch (e : ParseException) {
            throw RelicParseException(response, e)
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

    // endregion interface methods

    /**
     * Parse the response from the api and store the comments in the room db
     * @param response json string response
     * @param postFullName full name of post used as a key for the "after" value
     * @param parentDepth depth of the parent. Since posts start with a depth of 0, -1 is the
     * depth of the parent when calling from outside a recursive call
     * @param parentPosition positional value of the parent
     * @return : Parsed comment
     */
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
        val commentList = ArrayList<CommentModel>()

        // used for calculating the position of a comment
        val scale = 10f.pow(-(parentDepth + 1))
        var childCount = 1


        coroutineScope {
            commentChildren.forEach { commentChild ->
                val position = parentPosition + childCount * scale
                val commentJson = commentChild as JSONObject
                val childKind = commentJson["kind"] as String?

                if (childKind == "more") {
//                    // means there is a "more object"
//                    val deferredMore = async {
//                        val moreData = (commentJson["data"] as JSONObject)
//                        unmarshallMore(moreData, postFullName, position)
//                    }
//                    commentList.add(deferredMore.await())
                } else {
//                    val deferredCommentList = async {
//                        unmarshallComment(commentJson, position)
//                    }
//                    commentList.addAll(deferredCommentList.await())
                }

                childCount++
            }
        }

        return ParsedCommentData(listing, commentList, commentChildren.size)
    }

    private fun failParse(response : String, e : Throwable) : Nothing {
        throw RelicParseException(response, e)
    }

    // removes the type associated with the comment, leaving only its id
    override fun removeTypePrefix(fullName : String) : String {
        return if (fullName.length >= 4) {
            fullName.removeRange(0, 3)
        } else {
            ""
        }
    }

}