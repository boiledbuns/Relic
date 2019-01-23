package com.relic.data.deserializer

import android.text.Html
import com.google.gson.GsonBuilder
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

// TODO convert to object and add interface so this can be injected
object CommentDeserializer {
    private val gson = GsonBuilder().create()
    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.CANADA)
    private val currentYear = Date().year

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
    suspend fun parseComments(
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

    // TODO refactor and move the method into a comment entity method
    // TODO find a better way to unmarshall these objects and clean this up
    // won't be cleaned for a while because still decided how to format data and what is needed
    suspend fun unmarshallComment(
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

    private fun formatDate(epochTime : Double) : String? {
        val commentCreated = Date(epochTime.toLong() * 1000)

        return if (currentYear != commentCreated.year) {
            // add year if the comment wasn't made in the current year
            "${commentCreated.year} ${formatter.format(commentCreated)}"
        } else {
            formatter.format(commentCreated)
        }
    }

    // removes the type associated with the comment, leaving only its id
    fun removeTypePrefix(fullName : String) : String = fullName.removeRange(0, 3)

}

data class ParsedCommentData(
    val listingEntity : ListingEntity,
    val commentList : List<CommentEntity>,
    val replyCount : Int
)