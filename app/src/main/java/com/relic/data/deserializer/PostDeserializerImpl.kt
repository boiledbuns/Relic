package com.relic.data.deserializer

import android.content.Context
import android.text.Html
import android.util.Log
import com.google.gson.GsonBuilder
import com.relic.data.ApplicationDB
import com.relic.data.entities.CommentEntity
import com.relic.data.entities.ListingEntity
import com.relic.data.entities.PostEntity
import com.relic.data.entities.PostSourceEntity
import com.relic.data.PostRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class PostDeserializerImpl(
    appContext: Context
) : Contract.PostDeserializer {

    private val appDB: ApplicationDB = ApplicationDB.getDatabase(appContext)

    private val TYPE_POST = "t3"
    private val TAG = "POST_DESERIALIZER"

    private val jsonParser: JSONParser = JSONParser()
    private val gson = GsonBuilder().create()

    // initialize the date formatter and date for "now"
    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.CANADA)
    private val current = Date()

    override suspend fun parsePost(response: String) : PostEntity = coroutineScope {
        val data = ((jsonParser.parse(response) as JSONArray)[0] as JSONObject)["data"] as JSONObject
        val child = (data["children"] as JSONArray)[0] as JSONObject
        val post = child["data"] as JSONObject

        extractPost(post).apply {
            visited = true
        }
    }

    /**
     * Parses the response from the api and stores the posts in the persistence layer
     * TODO consider creating super class for posts and comments -> allows this method to return both
     * TODO separate into two separate methods and switch to mutithreaded to avoid locking main thread
     * @param response the json response from the server with the listing object
     * @throws ParseException
     */
    @Throws(ParseException::class)
    override suspend fun parsePosts(
        response: String,
        postSource: PostRepository.PostSource,
        listingKey : String
    ) : ParsedPostsData = coroutineScope {
        val listingData = (jsonParser.parse(response) as JSONObject)["data"] as JSONObject?
        val listingPosts = listingData!!["children"] as JSONArray?

        // create the new listing entity
        val listing = ListingEntity(listingKey, listingData["after"] as String?)

        val postIterator = listingPosts!!.iterator()
        val postEntities = ArrayList<PostEntity>()
        val commentEntities = ArrayList<CommentEntity>()

        val postSourceEntities = ArrayList<PostSourceEntity>()

        launch {
            var postCount: Int = getSourceCount(postSource)

            // generate the list of posts using the json array
            while (postIterator.hasNext()) {
                val fullEntityJson = (postIterator.next() as JSONObject)

                try {
                    var postSourceEntity: PostSourceEntity?
                    val postKind = fullEntityJson["kind"] as String

                    if (postKind.equals(TYPE_POST)) {

                        val post = fullEntityJson["data"] as JSONObject
                        val newPost = extractPost(post)
                        postEntities.add(newPost)

                        val existingPostSource = async {
                            appDB.postSourceDao.getPostSource(newPost.name)
                        }.await()

                        postSourceEntity = existingPostSource?.apply {
                            sourceId = newPost.name
                            subreddit = newPost.subreddit
                        } ?: PostSourceEntity(newPost.name, newPost.subreddit)
                    } else {
                        val newComment = CommentDeserializer.unmarshallComment(
                            commentChild = fullEntityJson,
                            postFullName = "",
                            commentPosition = 0F
                        ).first()
                        commentEntities.add(newComment)

                        postSourceEntity = PostSourceEntity(newComment.id, newComment.subreddit)
                    }

                    setSource(postSourceEntity, postSource, postCount)
                    postSourceEntities.add(postSourceEntity)

                    postCount++

                } catch (e : Exception) {
                    Log.d(TAG, "Error parsing post ${e.message} : \n $fullEntityJson")
                }
            }
        }.join()

        ParsedPostsData(postSourceEntities,postEntities, commentEntities, listing)
    }

    /**
     * This is fine for now because I'm still working on finalizing which fields to use/not use
     * There will be a lot more experimentation and changes to come in this method as a result
     */
    @Throws(ParseException::class)
    fun extractPost(post: JSONObject) : PostEntity {
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
            Log.d(TAG, "epoch = " + post["created"]!!)
            val apiCreated = Date((post["created"] as Double).toLong() * 1000)
            created = if (current.year != apiCreated.year) {
                apiCreated.year.toString() + " " + formatter.format(apiCreated)
            } else {
                formatter.format(apiCreated)
            }

            // get the gildings
            (post["gildings"] as JSONObject?)?.let { gilding ->
                platinum = (gilding["gid_1"] as Long).toInt()
                gold = (gilding["gid_2"] as Long).toInt()
                silver = (gilding["gid_3"] as Long).toInt()
            }
        }
    }

    private fun setSource(entity : PostSourceEntity, src: PostRepository.PostSource, position : Int) {
        entity.apply {
            when (src) {
                is PostRepository.PostSource.Subreddit -> {
                    subredditPosition = position
                }
                is PostRepository.PostSource.Frontpage -> {
                    frontpagePosition = position
                }
                is PostRepository.PostSource.All -> {
                    allPosition = position
                }
                is PostRepository.PostSource.User -> {
                    when (src.retrievalOption) {
                        PostRepository.RetrievalOption.Submitted -> userSubmittedPosition = position
                        PostRepository.RetrievalOption.Comments -> userCommentsPosition = position
                        PostRepository.RetrievalOption.Saved -> userSavedPosition = position
                        PostRepository.RetrievalOption.Upvoted -> userUpvotedPosition = position
                        PostRepository.RetrievalOption.Downvoted -> userDownvotedPosition = position
                        PostRepository.RetrievalOption.Gilded -> userGildedPosition = position
                        PostRepository.RetrievalOption.Hidden -> userHiddenPosition = position
                    }
                }
            }
        }
    }

    private fun getSourceCount(postSource : PostRepository.PostSource) : Int {
        val sourceDao = appDB.postSourceDao

        return when (postSource) {
            is PostRepository.PostSource.Subreddit -> sourceDao.getItemsCountForSubreddit(postSource.subredditName)
            is PostRepository.PostSource.Frontpage -> sourceDao.getItemsCountForFrontpage()
            is PostRepository.PostSource.All -> sourceDao.getItemsCountForAll()
            is PostRepository.PostSource.Popular -> 0
            is PostRepository.PostSource.User -> {
                when (postSource.retrievalOption) {
                    PostRepository.RetrievalOption.Submitted -> sourceDao.getItemsCountForUserSubmitted()
                    PostRepository.RetrievalOption.Comments -> sourceDao.getItemsCountForUserComments()
                    PostRepository.RetrievalOption.Saved -> sourceDao.getItemsCountForUserSaved()
                    PostRepository.RetrievalOption.Upvoted -> sourceDao.getItemsCountForUserUpvoted()
                    PostRepository.RetrievalOption.Downvoted -> sourceDao.getItemsCountForUserDownvoted()
                    PostRepository.RetrievalOption.Gilded -> sourceDao.getItemsCountForUserGilded()
                    PostRepository.RetrievalOption.Hidden -> sourceDao.getItemsCountForUserHidden()
                }
            }
        }
    }
}