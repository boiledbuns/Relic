package com.relic.data.deserializer

import android.text.Html
import android.util.Log
import com.google.gson.GsonBuilder
import com.relic.api.response.Listing
import com.relic.data.CommentsAndPostData
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.squareup.moshi.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
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


    private val commentType = Types.newParameterizedType(Listing::class.java, CommentModel::class.java)
    private val commentListingAdapter = moshi.adapter<Listing<CommentModel>>(commentType)

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
        return try {
            // the comment data is nested as the second element within an array
            val parentChildList = jsonParser.parse(response) as JSONArray

            Timber.log(0, "parse comments child ${parentChildList[1].toString()}")
            val postListing = postListingAdapter.fromJson(parentChildList[0].toString()) ?: throw RelicParseException(response)
            val commentListing= commentListingAdapter.fromJson(parentChildList[1].toString()) ?: throw RelicParseException(response)

            CommentsAndPostData(postListing.data.children!!.first(), commentListing)
        } catch (e : ParseException) {
            throw RelicParseException(response, e)
        }
    }

//    inner class CommentResponseAdapter() {
//        @FromJson
//
//        @ToJson
//        fun toJson
//    }


    /**
     * Only use this method to parse the return from "morechildren" since it uses a different
     * format than the traditional method for retrieving comments
     *
     */
    override suspend fun parseMoreCommentsResponse(
        moreChildrenComment: CommentModel,
        response: String
    ) : List<CommentEntity> {
        val requestJson = (jsonParser.parse(response) as JSONObject)["json"] as JSONObject

        val requestData = requestJson["data"] as JSONObject
        val requestComments = requestData["things"] as JSONArray

        // calculate the depth of the comments (should be the same as the "load more")
        val depth = moreChildrenComment.depth
        val scale = 10f.pow(-(depth))
        var commentCount = 0

        Log.d(TAG, "load more scale ${moreChildrenComment.position}")

        return try {
            requestComments.fold(mutableListOf()) { accum, requestComment : Any? ->
                val commentJson = requestComment as JSONObject
                val childKind = commentJson["kind"] as String?

                val unmarshalledComments = if (childKind == "more") {
                    // means there is a "more object"
                    val moreData = (commentJson["data"] as JSONObject)

                    val moreComment = unmarshallMore(
                        moreData,
                        moreChildrenComment.parentPostId,
                        moreChildrenComment.position + commentCount*scale
                    )
                    commentCount += 1
                    listOf(moreComment)
                } else {
                    unmarshallComment(commentJson, moreChildrenComment.depth.toFloat()).apply {
                        forEach { commentEntity ->
                            commentEntity.position = moreChildrenComment.position + commentCount*scale
                            commentCount += 1
                            Log.d(TAG, "load more scale ${commentEntity.position}")
                        }
                    }
                }

                accum.apply { addAll(unmarshalledComments) }
            }
        } catch (e : ParseException) {
            throw RelicParseException(response, e)
        }
    }

    // TODO refactor and move the method into a comment entity method
    // TODO find a better way to unmarshall these objects and clean this up
    // won't be cleaned for a while because still decided how to format data and what is needed
    override suspend fun unmarshallComment(
        commentChild : JSONObject,
        commentPosition : Float
    ) : List<CommentEntity> {
        val commentPOJO = commentChild["data"] as JSONObject
        val commentList = ArrayList<CommentEntity>()

        coroutineScope {

            var deferredCommentData: Deferred<ParsedCommentData>? = null
            val commentEntity = gson.fromJson(commentPOJO.toString(), CommentEntity::class.java).apply {

                position = commentPosition
                val parentPostId = removeTypePrefix(commentPOJO["link_id"] as String)
                this.parentPostId = parentPostId

                commentPOJO["replies"]?.let { childJson ->
                    // try to parse the child json as nested replies
                    if (childJson.toString().isNotEmpty()) {
                        // parse the children of this comment
                        deferredCommentData = async {
                            parseComments(
                                postFullName = parentPostId,
                                response = childJson as JSONObject,
                                parentDepth = depth,
                                parentPosition = commentPosition
                            )
                        }
                    }
                }
                submitter = commentPOJO["is_submitter"] as Boolean
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
                    (gilding["gid_1"] as Long?)?.let { platinum = it.toInt() }
                    (gilding["gid_2"] as Long?)?.let { gold = it.toInt() }
                    (gilding["gid_3"]as Long?)?.let { silver = it.toInt() }
                }

                // have to do this because Reddit has a decided this can be boolean or string
                try {
                    editedDate = formatDate(commentPOJO["edited"] as Double)
                } catch (e: Exception) {

                }

                if (author == null) {
                    Log.d(TAG, "author is null")
                }
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
        val commentList = ArrayList<CommentEntity>()

        // used for calculating the position of a comment
        val scale = 10f.pow(-(parentDepth + 1))
        var childCount = 1


        coroutineScope {
            commentChildren.forEach { commentChild ->
                val position = parentPosition + childCount * scale
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
                        unmarshallComment(commentJson, position)
                    }
                    commentList.addAll(deferredCommentList.await())
                }

                childCount++
            }
        }

        return ParsedCommentData(listing, commentList, commentChildren.size)
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

            val childrenLinks = moreJsonObject["children"] as JSONArray
            body_html = childrenLinks.toString()
            // reply count for "more" item will hold the number of comments to load
            replyCount = childrenLinks.size
        }
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