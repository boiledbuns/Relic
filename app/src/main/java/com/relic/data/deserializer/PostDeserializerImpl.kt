package com.relic.data.deserializer

import android.text.Html
import android.util.Log
import com.google.gson.GsonBuilder
import com.relic.data.*
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.data.entities.PostEntity
import com.relic.data.entities.PostSourceEntity
import com.relic.domain.models.PostModel
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.lang.Exception
import java.text.SimpleDateFormat
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

    // initialize the date formatter and date for "now"
    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.CANADA)
    private val current = Date()

    override suspend fun parsePost(response: String) : ParsedPostData {
        val data = ((jsonParser.parse(response) as JSONArray)[0] as JSONObject)["data"] as JSONObject
        val child = (data["children"] as JSONArray)[0] as JSONObject
        val post = child["data"] as JSONObject

        val newPost = try {
            extractPost(post)
        } catch (e : ParseException){
            throw RelicParseException(response, e)
        }
        // should probably be supplied instead of retrieved here
        val existingPostSource = withContext(Dispatchers.IO) {
            appDB.postSourceDao.getPostSource(newPost.name)
        }

        val postSourceEntity = existingPostSource?.apply {
            sourceId = newPost.name
            subreddit = newPost.subreddit
        } ?: PostSourceEntity(newPost.name, newPost.subreddit)

        return ParsedPostData(postSourceEntity, newPost)
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
    ) : ParsedPostsData = coroutineScope {
        val postEntities = ArrayList<PostEntity>()
        val commentEntities = ArrayList<CommentEntity>()
        val postSourceEntities = ArrayList<PostSourceEntity>()
        val listing = ListingEntity()

        val listingJSON = jsonParser.parse(response) as JSONObject
        val listingData = listingJSON["data"] as JSONObject?
        val listingPosts = listingData!!["children"] as JSONArray?

        // update listing entity fields
        listing.apply {
            this.listingKey = listingKey
            afterPosting = listingData["after"] as String?
        }

        val postIterator = listingPosts?.iterator()

        var postCount: Int = getSourceCount(postSource)

        // generate the list of posts using the json array
        while (postIterator != null && postIterator.hasNext()) {
            val fullEntityJson = (postIterator.next() as JSONObject)

            try {
                var postSourceEntity: PostSourceEntity?
                val postKind = fullEntityJson["kind"] as String

                if (postKind.equals(TYPE_POST)) {

                    val post = fullEntityJson["data"] as JSONObject
                    val newPost = extractPost(post)
                    postEntities.add(newPost)

                    val existingPostSource = async(Dispatchers.IO) {
                        appDB.postSourceDao.getPostSource(newPost.name)
                    }.await()

                    postSourceEntity = existingPostSource?.apply {
                        sourceId = newPost.name
                        subreddit = newPost.subreddit
                    } ?: PostSourceEntity(newPost.name, newPost.subreddit)
                } else {
                    val newComment = commentDeserializer.unmarshallComment(
                        commentChild = fullEntityJson,
                        commentPosition = 0F
                    ).first()
                    commentEntities.add(newComment)

                    postSourceEntity = PostSourceEntity(newComment.id, newComment.subreddit)
                }

                setSource(postSourceEntity, postSource, postCount)
                postSourceEntities.add(postSourceEntity)

                postCount++
                Log.d(TAG, "post count : $postCount")
            } catch (e : Exception) {
                throw RelicParseException(response, e)
            }
        }

        ParsedPostsData(postSourceEntities, postEntities, commentEntities, listing)
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

    /**
     * This is fine for now because I'm still working on finalizing which fields to use/not use
     * There will be a lot more experimentation and changes to come in this method as a result
     */
    private fun extractPost(post: JSONObject) : PostEntity {
        // use "api" prefix to indicate fields accessed directly from api
        return gson.fromJson(post.toJSONString(), PostEntity::class.java).apply {
            //Log.d(TAG, "post : " + post.get("title") + " "+ post.get("author"));
            //Log.d(TAG, "src : " + post.get("src") + ", media domain url = "+ post.get("media_domain_url"));
            //Log.d(TAG, "media embed : " + post.get("media_embed") + ", media = "+ post.get("media"));
            //Log.d(TAG, "preview : " + post.get("preview") + " "+ post.get("url"));
            Log.d(TAG, "link_flair_richtext : " + post["score"] + " " + post["ups"] + " " + post["wls"] + " " + post["likes"])
            //Log.d(TAG, "link_flair_richtext : " + post.get("visited") + " "+ post.get("views") + " "+ post.get("pwls") + " "+ post.get("gilded"));
            //Log.d(TAG, "post keys " + post.keySet().toString())
            // unmarshall the object and add it into a list

            val apiLikes = post["likes"] as Boolean?
            userUpvoted = if (apiLikes == null) 0 else if (apiLikes) 1 else -1

            // TODO create parse class/switch to a more efficient method of removing html
            val authorFlair = post["author_flair_text"] as String?
            author_flair_text = if (authorFlair != null && !authorFlair.isEmpty()) {
                Html.fromHtml(authorFlair).toString()
            } else null

            // add year to stamp if the post year doesn't match the current one
            Log.d(TAG, "epoch = " + post["created"])
            val apiCreated = Date((post["created"] as Double).toLong() * 1000)
            created = if (current.year != apiCreated.year) {
                apiCreated.year.toString() + " " + formatter.format(apiCreated)
            } else {
                formatter.format(apiCreated)
            }

            // get the gildings
            (post["gildings"] as JSONObject?)?.let { gilding ->
                (gilding["gid_1"] as Long?)?.let { platinum = it.toInt() }
                (gilding["gid_2"] as Long?)?.let { gold = it.toInt() }
                (gilding["gid_3"] as Long?)?.let { silver = it.toInt() }
            }
        }
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