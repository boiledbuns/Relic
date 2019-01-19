package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.text.Html
import android.util.Log
import com.android.volley.NoConnectionError

import com.google.gson.GsonBuilder
import com.relic.R
import com.relic.network.NetworkRequestManager
import com.relic.data.gateway.PostGateway
import com.relic.data.gateway.PostGatewayImpl
import com.relic.network.request.RelicOAuthRequest
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.data.entities.ListingEntity
import com.relic.data.entities.PostEntity
import com.relic.data.entities.PostSourceEntity
import com.relic.data.models.PostModel
import com.relic.network.request.RelicRequestError
import kotlinx.coroutines.*

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.lang.Exception

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

/**
 * This repository is used for accessing posts either by:
 * a) getting a livedata reference for locally stored posts
 * b) retrieving posts from the network and storing them locally
 *
 * Note: Cancellation of parent coroutines automatically propagate downward to
 * all its children. When performing retrieval methods (eg. post retrieval) from
 * the network, the process should not be cancelled even if its parent coroutine
 * is cancelled.
 *
 * I initially designed the retrieval methods as non-suspending functions that internally
 * launched a coroutine from the Global scope but realized suspend functions offered more
 * benefits:
 * a) The method is poorly thought out and makes cancellation more difficult than necessary
 * b) Because the new methods are now suspending, the caller has control over the coroutine
 * scope. This is certainly more flexible as there are some (though rare) cases where we can
 * supply a non-global scope that we can close at our own convenience to cancel retrieval
 *
 *  As a result, the retrieval methods are now suspend functions
 */
class PostRepositoryImpl @Inject constructor(
    private val appContext: Context,
    private val requestManager: NetworkRequestManager
) : PostRepository {

    companion object {
        private const val ENDPOINT = "https://oauth.reddit.com/"
        private const val TAG = "POST_REPO"

        // keys for the "after" value for listings
        private const val KEY_FRONTPAGE = "frontpage"
        private const val KEY_ALL = "all"
        private const val KEY_OTHER = "other"
    }

    private val sortTypesWithScope = arrayOf(
        PostRepository.SortType.HOT,
        PostRepository.SortType.RISING,
        PostRepository.SortType.TOP
    )

    private val jsonParser: JSONParser = JSONParser()
    private val appDB: ApplicationDB = ApplicationDB.getDatabase(appContext)

    private val gson = GsonBuilder().create()
    // initialize the date formatter and date for "now"
    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a")
    private val current = Date()

    override val postGateway: PostGateway
        get() = PostGatewayImpl(appContext, requestManager)

    // get the oauth token from the app's shared preferences
    private fun checkToken(): String {
        // retrieve the auth token shared preferences
        val authKey = appContext.resources.getString(R.string.AUTH_PREF)
        val tokenKey = appContext.resources.getString(R.string.TOKEN_KEY)
        return appContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
            .getString(tokenKey, "DEFAULT") ?: ""
    }

    // region interface methods

    override fun getPosts(postSource: PostRepository.PostSource) : LiveData<List<PostModel>> {
        return when (postSource) {
            is PostRepository.PostSource.Subreddit -> appDB.postDao.getPostsFromSubreddit(postSource.subredditName)
            is PostRepository.PostSource.Frontpage -> appDB.postDao.getPostsFromFrontpage()
            is PostRepository.PostSource.User -> {
                when (postSource.retrievalOption) {
                    PostRepository.RetrievalOption.Submitted -> appDB.postDao.getUserPosts(postSource.username)
                    PostRepository.RetrievalOption.Comments -> appDB.postDao.getUserPosts(postSource.username)
                    PostRepository.RetrievalOption.Saved -> appDB.postDao.getUserSavedPosts()
                    PostRepository.RetrievalOption.Upvoted -> appDB.postDao.getUserVotedPosts(1)
                    PostRepository.RetrievalOption.Downvoted -> appDB.postDao.getUserVotedPosts(-1)
                    PostRepository.RetrievalOption.Gilded -> appDB.postDao.getUserGilded()
                    PostRepository.RetrievalOption.Hidden -> appDB.postDao.getUserPosts(postSource.username)
                }
            }
            else -> appDB.postDao.getPostsFromAll()
        }
    }

    override suspend fun retrieveMorePosts(
        postSource: PostRepository.PostSource,
        listingAfter: String
    ) {
        // change the api endpoint to access the next post listing
        val ending = when (postSource) {
            is PostRepository.PostSource.Subreddit -> "r/${postSource.subredditName}"
            is PostRepository.PostSource.User -> "user/${postSource.username}"
            else -> ""
        }

        try {
            // create the new request and submit it
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = "$ENDPOINT$ending?after=$listingAfter",
                authToken = checkToken()
            )
            parsePosts(response, postSource)
        } catch (e : Exception) {
            Log.d(TAG, "Error: " + e.message)
        }

    }

    /**
     * Retrieves the "after" values to be used for the next post listing
     * @param callback callback to send the name to
     * @param postSource source of the post
     */
    override fun getNextPostingVal(callback: RetrieveNextListingCallback, postSource: PostRepository.PostSource) {
        val key = getListingKey(postSource)
        RetrieveListingAfterTask(appDB, callback).execute(key)
    }

    override fun getPost(postFullName: String): LiveData<PostModel> {
        return appDB.postDao.getSinglePost(postFullName)
    }

    override suspend fun retrieveSortedPosts(
        postSource: PostRepository.PostSource,
        sortType: PostRepository.SortType,
        sortScope: PostRepository.SortScope
    ) {
        // generate the ending of the request url based on the source type
        var ending = ENDPOINT + when (postSource) {
            is PostRepository.PostSource.Subreddit -> "r/${postSource.subredditName}"
            is PostRepository.PostSource.User -> {
                "user/${postSource.username}/${postSource.retrievalOption.name}"
            }
            else -> ""
        }

        // modify the endpoint based on the sorting options selected by the user
        if (sortType != PostRepository.SortType.DEFAULT) {
            // build the appropriate endpoint based on the "sort by" code and time scope
            ending += "/${sortType.name.toLowerCase()}/"

            // only add sort scope for the options that accept it
            if (sortTypesWithScope.contains(sortType)) ending += "?t=" + sortScope.name.toLowerCase()
        }

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = ending,
                authToken = checkToken()
            )
            parsePosts(response, postSource)
        } catch (e : Exception) {
            Log.d(TAG, "Error retrieving sorted posts $e")
        }
    }

    override suspend fun retrievePost(
        subredditName: String,
        postFullName: String,
        postSource: PostRepository.PostSource,
        errorHandler: (error : RelicRequestError) -> Unit
    ) {
        val ending = "r/$subredditName/comments/${postFullName.substring(3)}"

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = ENDPOINT + ending,
                authToken = checkToken()
            )
            parsePost(response)
        } catch (e :Exception) {
            // TODO decide if it would be better to move this to another method
            when (e) {
                is NoConnectionError -> {
                    errorHandler.invoke(RelicRequestError.NetworkUnavailableError())
                }
                else -> Log.d(TAG, "Error retrieving post: $e")
            }
        }
    }

    override suspend fun clearAllPostsFromSource(postSource: PostRepository.PostSource) {
        ClearPostsFromSourceTask().execute(appDB, postSource)
    }

    // endregion interface methods

    // region helper functions

    /**
     * Parses the response from the api and stores the posts in the persistence layer
     * TODO separate into two separate methods and switch to mutithreaded to avoid locking main thread
     * @param response the json response from the server with the listing object
     * @throws ParseException
     */
    @Throws(ParseException::class)
    private suspend fun parsePosts(
        response: String,
        postSource: PostRepository.PostSource
    ) : List<PostEntity> = coroutineScope {
        val listingData = (jsonParser.parse(response) as JSONObject)["data"] as JSONObject?
        val listingPosts = listingData!!["children"] as JSONArray?

        val listingKey = getListingKey(postSource)

        // create the new listing entity
        val listing = ListingEntity(listingKey, listingData["after"] as String?)

        val postIterator = listingPosts!!.iterator()
        val postEntities = ArrayList<PostEntity>()
        val postSourceEntities = ArrayList<PostSourceEntity>()

        async {
            val sourceDao = appDB.postSourceDao

            var postCount: Int = when (postSource) {
                is PostRepository.PostSource.Subreddit -> sourceDao.getItemsCountForSubreddit(postSource.subredditName)
                is PostRepository.PostSource.Frontpage -> sourceDao.getItemsCountForFrontpage()
                is PostRepository.PostSource.All -> sourceDao.getItemsCountForAll()
                is PostRepository.PostSource.Popular -> 0
                is PostRepository.PostSource.User -> sourceDao.getItemsCountForUserSubmission()
            }

            // generate the list of posts using the json array
            while (postIterator.hasNext()) {
                val post = (postIterator.next() as JSONObject)["data"] as JSONObject
                val newPost = extractPost(post)
                postEntities.add(newPost)

                val postSourceEntity = PostSourceEntity(newPost.name, newPost.subreddit)
                postSourceEntities.add(postSourceEntity)

                val existingPostSource = async {
                    appDB.postSourceDao.getPostSource(newPost.name)
                }.await()

                if (existingPostSource != null) {
                    postSourceEntity.apply {
                        subredditPosition = existingPostSource.subredditPosition
                        frontpagePosition= existingPostSource.frontpagePosition
                        allPosition = existingPostSource.allPosition
                        userSubmissionPosition = existingPostSource.userSubmissionPosition
                    }
                }

                when (postSource) {
                    is PostRepository.PostSource.Subreddit -> {
                        postSourceEntity.subredditPosition = postCount
                    }
                    is PostRepository.PostSource.Frontpage -> {
                        postSourceEntity.frontpagePosition = postCount
                    }
                    is PostRepository.PostSource.All -> {
                        postSourceEntity.allPosition = postCount
                    }
                    is PostRepository.PostSource.User -> {
                        postSourceEntity.userSubmissionPosition = postCount
                    }
                }

                postCount ++
            }

            appDB.postDao.insertPosts(postEntities)
            appDB.postSourceDao.insertPostSources(postSourceEntities)
            appDB.listingDAO.insertListing(listing)
        }.await()

        postEntities
    }

    private suspend fun parsePost(response: String) = coroutineScope {
        val data = ((jsonParser.parse(response) as JSONArray)[0] as JSONObject)["data"] as JSONObject
        val child = (data["children"] as JSONArray)[0] as JSONObject
        val post = child["data"] as JSONObject

        launch {
            val postEntity = extractPost(post).apply {
                visited = true
            }

            InsertPostTask().execute(appDB, postEntity)
        }
    }

    /**
     * This is fine for now because I'm still working on finalizing which fields to use/not use
     * There will be a lot more experimentation and changes to come in this method as a result
     */
    @Throws(ParseException::class)
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

    /**
     * builds the key used for retrieving the "after" value for a listing using its associated
     * post source
     */
    private fun getListingKey(postSource : PostRepository.PostSource) : String {
        return when (postSource) {
            is PostRepository.PostSource.Subreddit -> postSource.subredditName
            is PostRepository.PostSource.Frontpage -> KEY_FRONTPAGE
            is PostRepository.PostSource.All -> KEY_ALL
            is PostRepository.PostSource.User -> postSource.username + postSource.retrievalOption.name
            else -> KEY_OTHER
        }
    }

    // end region helper functions

    // region async tasks

    /**
     * Async task to insert posts and create/update the listing data for the current subreddit to
     * point to the next listing
     */
    internal class InsertPostsTask(
        private var appDB: ApplicationDB,
        private var postList: List<PostEntity>,
        private var postSourceEntities : ArrayList<PostSourceEntity>
    ) : AsyncTask<ListingEntity, Int, Int>() {

        override fun doInBackground(vararg listing: ListingEntity): Int? {
            appDB.postDao.insertPosts(postList)
            appDB.postSourceDao.insertPostSources(postSourceEntities)
            appDB.listingDAO.insertListing(listing[0])
            return null
        }
    }

    private class InsertPostTask : AsyncTask<Any, Unit, Unit>() {
        override fun doInBackground(vararg objects: Any) {
            val applicationDB = objects[0] as ApplicationDB
            applicationDB.postDao.insertPost(objects[1] as PostEntity)
        }
    }

    private class RetrieveListingAfterTask(
        var appDB: ApplicationDB,
        var callback: RetrieveNextListingCallback
    ) : AsyncTask<String, Unit, Unit>() {
        override fun doInBackground(vararg strings: String) {
            // get the "after" value for the most current sub listing
            val subAfter = appDB.listingDAO.getNext(strings[0])
            callback.onNextListing(subAfter)
        }
    }

    private class ClearPostsFromSourceTask : AsyncTask<Any, Unit, Unit>() {
        override fun doInBackground(vararg objects: Any) {
            val appDB = objects[0] as ApplicationDB
            val postSource = objects[1] as PostRepository.PostSource
            when (postSource) {
                is PostRepository.PostSource.Frontpage -> {
                    appDB.postSourceDao.removeAllFrontpageAsSource()
                }
                is PostRepository.PostSource.All -> {
                    appDB.postSourceDao.removeAllAllAsSource()
                }
                is PostRepository.PostSource.Subreddit -> {
                    appDB.postSourceDao.removeAllSubredditAsSource(postSource.subredditName)
                }
                is PostRepository.PostSource.User -> {
                    appDB.postSourceDao.removeAllCurrentUserAsSource()
                }
            }
            // remove all the source entities that no longer correspond to any remaining posts
            appDB.postSourceDao.removeAllUnusedSources()
        }
    }

    // endregion async tasks
}
