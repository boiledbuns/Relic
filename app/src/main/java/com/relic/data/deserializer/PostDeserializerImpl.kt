package com.relic.data.deserializer

import com.google.gson.GsonBuilder
import com.relic.api.response.Listing
import com.relic.data.ApplicationDB
import com.relic.data.PostSource
import com.relic.data.SubSearchResult
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
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

    override suspend fun parsePost(response: String) : PostModel {
        try {
            // api returns a listing with a single post item as its child
            return postListingAdapter.fromJson(response)!!.data.children!!.first()
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

    private suspend fun getSourceCount(postSource : PostSource) : Int {
        val sourceDao = appDB.postSourceDao

        return withContext(Dispatchers.IO) {
            sourceDao.getItemsCountForSource(postSource.getSourceName())
        }
    }
}