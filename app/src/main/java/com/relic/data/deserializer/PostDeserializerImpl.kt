package com.relic.data.deserializer

import com.google.gson.GsonBuilder
import com.relic.api.response.Listing
import com.relic.data.ApplicationDB
import com.relic.data.PostSource
import com.relic.data.RetrievalOption
import com.relic.data.SubSearchResult
import com.relic.data.entities.ListingEntity
import com.relic.data.entities.PostSourceEntity
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.util.*
import javax.inject.Inject

class PostDeserializerImpl @Inject constructor(
    private val appDB : ApplicationDB,
    private val commentDeserializer: Contract.CommentDeserializer,
    private val moshi : Moshi
) : Contract.PostDeserializer {

    private val TYPE_POST = "t3"
    private val TAG = "POST_DESERIALIZER"

    private val jsonParser: JSONParser = JSONParser()
    private val gson = GsonBuilder().create()

    private val postAdapter = moshi.adapter(PostModel::class.java)

    private val postType = Types.newParameterizedType(Listing::class.java, ListingItem::class.java)
    private val postListingAdapter = moshi.adapter<Listing<PostModel>>(postType)

    override suspend fun parsePost(response: String) : ParsedPostData {
        val data = ((jsonParser.parse(response) as JSONArray)[0] as JSONObject)["data"] as JSONObject
        val child = (data["children"] as JSONArray)[0] as JSONObject

        try {
            val newPost = postAdapter.fromJson(child.toJSONString()) ?: throw RelicParseException(response)

            // should probably be supplied instead of retrieved here
            val existingPostSource = withContext(Dispatchers.IO) {
                appDB.postSourceDao.getPostSource(newPost.fullName)
            }

            val postSourceEntity = existingPostSource?.apply {
                sourceId = newPost.fullName
                subreddit = newPost.subreddit!!
            } ?: PostSourceEntity(newPost.fullName, newPost.subreddit!!)

            return ParsedPostData(postSourceEntity, newPost)

        } catch (e : ParseException){
            throw RelicParseException(response, e)
        }
    }

    /**
     * Parses the response from the api and stores the posts in the persistence layer
     * TODO consider creating super class for posts and comments -> allows this method to return both
     * TODO separate into two separate methods and switch to mutithreaded to avoid locking main thread
     * @param response the json response from the server with the listing object
     * @throws ParseException
     */
    override suspend fun parsePosts(
        response: String,
        postSource: PostSource,
        listingKey : String
    ) : Listing<PostModel> {
        try {
            return postListingAdapter.fromJson(response)!!
        } catch (e : Exception) {
            throw RelicParseException(response, e)
        }
    }

    override suspend fun parseSearchSubPostsResponse(response: String): SubSearchResult {
        // TODO realized that we can do this with gson so look to replace simplejson with it
        val listing = jsonParser.parse(response) as JSONObject
        val data = listing["data"] as JSONObject
        val children = data["children"] as JSONArray

        val after = data["after"] as String?
        val postModels = children.mapNotNull { mapToPostModel(it as JSONObject) }

        return SubSearchResult(postModels, after)
    }

    private fun mapToPostModel(child : JSONObject) : PostModel? {
        return moshi.adapter(PostModel::class.java)
            .fromJson((child["data"] as JSONObject).toString())
    }

    private fun setSource(entity : PostSourceEntity, src: PostSource, position : Int) {
        entity.apply {
            when (src) {
                is PostSource.Subreddit -> {
                    subredditPosition = position
                }
                is PostSource.Frontpage -> {
                    frontpagePosition = position
                }
                is PostSource.All -> {
                    allPosition = position
                }
                is PostSource.User -> {
                    when (src.retrievalOption) {
                        RetrievalOption.Submitted -> userSubmittedPosition = position
                        RetrievalOption.Comments -> userCommentsPosition = position
                        RetrievalOption.Saved -> userSavedPosition = position
                        RetrievalOption.Upvoted -> userUpvotedPosition = position
                        RetrievalOption.Downvoted -> userDownvotedPosition = position
                        RetrievalOption.Gilded -> userGildedPosition = position
                        RetrievalOption.Hidden -> userHiddenPosition = position
                    }
                }
            }
        }
    }

    private suspend fun getSourceCount(postSource : PostSource) : Int {
        val sourceDao = appDB.postSourceDao

        return withContext(Dispatchers.IO) {
            when (postSource) {
                is PostSource.Subreddit -> sourceDao.getItemsCountForSubreddit(postSource.subredditName)
                is PostSource.Frontpage -> sourceDao.getItemsCountForFrontpage()
                is PostSource.All -> sourceDao.getItemsCountForAll()
                is PostSource.Popular -> 0
                is PostSource.User -> {
                    when (postSource.retrievalOption) {
                        RetrievalOption.Submitted -> sourceDao.getItemsCountForUserSubmitted()
                        RetrievalOption.Comments -> sourceDao.getItemsCountForUserComments()
                        RetrievalOption.Saved -> sourceDao.getItemsCountForUserSaved()
                        RetrievalOption.Upvoted -> sourceDao.getItemsCountForUserUpvoted()
                        RetrievalOption.Downvoted -> sourceDao.getItemsCountForUserDownvoted()
                        RetrievalOption.Gilded -> sourceDao.getItemsCountForUserGilded()
                        RetrievalOption.Hidden -> sourceDao.getItemsCountForUserHidden()
                    }
                }
            }
        }
    }
}