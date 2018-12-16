package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.text.Html
import android.util.Log
import com.android.volley.Response

import com.google.gson.GsonBuilder
import com.relic.R
import com.relic.network.NetworkRequestManager
import com.relic.data.gateway.PostGateway
import com.relic.data.gateway.PostGatewayImpl
import com.relic.network.request.RelicOAuthRequest
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.data.entities.ListingEntity
import com.relic.data.entities.PostEntity
import com.relic.data.entities.PostEntity.ORIGIN_ALL
import com.relic.data.entities.PostEntity.ORIGIN_FRONTPAGE
import com.relic.data.models.PostModel

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val currentContext: Context,
    private val requestManager: NetworkRequestManager
) : PostRepository {

    companion object {
        private const val ENDPOINT = "https://oauth.reddit.com/"
        private const val userAgent = "android:com.relic.Relic (by /u/boiledbuns)"
        private const val TAG = "POST_REPO"

        private const val KEY_FRONTPAGE = "frontpage"
        private const val KEY_ALL = "all"
    }

    private val jsonParser: JSONParser = JSONParser()
    private val appDB: ApplicationDB = ApplicationDB.getDatabase(currentContext)

    private val gson = GsonBuilder().create()
    // initialize the date formatter and date for "now"
    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a")
    private val current = Date()

    override val postGateway: PostGateway
        get() = PostGatewayImpl(currentContext, requestManager)

    // get the oauth token from the app's shared preferences
    private fun checkToken(): String {
        // retrieve the auth token shared preferences
        val authKey = currentContext.resources.getString(R.string.AUTH_PREF)
        val tokenKey = currentContext.resources.getString(R.string.TOKEN_KEY)
        return currentContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
            .getString(tokenKey, "DEFAULT") ?: ""
    }

    // region interface methods

    override fun getPosts(postSource: PostRepository.PostSource) : LiveData<List<PostModel>> {
        return when (postSource) {
            is PostRepository.PostSource.Subreddit -> appDB.postDao.getSubredditPosts(postSource.subredditName)
            is PostRepository.PostSource.Frontpage -> appDB.postDao.getPostsFromOrigin(ORIGIN_FRONTPAGE)
            else -> appDB.postDao.getPostsFromOrigin(ORIGIN_ALL)
        }
    }

    override fun retrieveMorePosts(postSource: PostRepository.PostSource, after: String) {
        // change the api endpoint to access to get the next post listing
        val ending =  when (postSource) {
            is PostRepository.PostSource.Subreddit -> "r/${postSource.subredditName}"
            else -> ""
        }

        // create the new request and submit it
        requestManager.processRequest(
            RelicOAuthRequest(
                RelicOAuthRequest.GET,
                "$ENDPOINT$ending?after=$after",
                Response.Listener { response ->
                    try {
                        parsePosts(response, postSource)
                    } catch (error: ParseException) {
                        Log.d(TAG, "Error: " + error.message)
                    }
                },
                Response.ErrorListener { error -> Log.d(TAG, "Error: " + error.message) },
                checkToken()
            )
        )
    }

    /**
     * Retrieves the "after" values to be used for the next post listing
     * @param callback callback to send the name to
     * @param postSource nsource of the post
     */
    override fun getNextPostingVal(callback: RetrieveNextListingCallback, postSource: PostRepository.PostSource) {
        val subName = when (postSource) {
            is PostRepository.PostSource.Subreddit -> postSource.subredditName
            is PostRepository.PostSource.Frontpage -> KEY_FRONTPAGE
            else -> KEY_ALL
        }

        RetrieveListingAfterTask(appDB, callback).execute(subName)
    }

    override fun getPost(postFullName: String): LiveData<PostModel> {
        return appDB.postDao.getSinglePost(postFullName)
    }

    /**
     * Deletes all locally stored posts and retrieves a new set based on the sorting method specified
     * by the caller
     * @param postSource source of the subreddit
     * @param sortType code for the associated sort by method
     * @param sortScope  code for the associate time span to sort by
     */
    override fun retrieveSortedPosts(
        postSource: PostRepository.PostSource,
        sortType: PostRepository.SortType,
        sortScope: PostRepository.SortScope
    ) {

        // generate the ending of the request url based on the source type
        var ending = ENDPOINT + when (postSource) {
            is PostRepository.PostSource.Subreddit -> "r/" + postSource.subredditName
            else -> ""
        }

        // change the endpoint based on which sorting option the user has selected
        if (sortType != PostRepository.SortType.DEFAULT) {
            // build the appropriate endpoint based on the "sort by" code and time scope
            ending += "/" + sortType.name.toLowerCase() + "/?sort=" + sortScope.name.toLowerCase()

            // only add sort scope for these sorting types
            if (sortType == PostRepository.SortType.HOT || sortType == PostRepository.SortType.RISING || sortType == PostRepository.SortType.TOP) {
                // add the scope only if the sorting type has one
                ending += "&t=" + sortScope.name.toLowerCase()
            }
        }

        Log.d(TAG, ending)
        requestManager.processRequest(
            RelicOAuthRequest(
                RelicOAuthRequest.GET,
                ending,
                Response.Listener { response: String ->
                    Log.d(TAG, response)
                    try {
                        parsePosts(response, postSource)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener { error -> Log.d(TAG, "Error retrieving sorted posts $error") },
                checkToken()
            )
        )
    }

    override fun retrievePost(subredditName: String, postFullName: String, postSource: PostRepository.PostSource) {
        val ending = ENDPOINT + "r/" + subredditName + "/comments/" + postFullName.substring(3)
        // create the new request and submit it
        requestManager.processRequest(
            RelicOAuthRequest(
                RelicOAuthRequest.GET,
                ending,
                Response.Listener { response ->
                    try {
                        parsePost(response, postSource)
                    } catch (error: ParseException) {
                        Log.d(TAG, "Error: " + error.message)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d(TAG, "Error: " + error.networkResponse)
                    // TODO add liveData for error
                    // TODO maybe retry if not an internet connection issue
                },
                checkToken()
            )
        )
    }

    override fun clearAllPostsFromSource(postSource: PostRepository.PostSource) {
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
    private fun parsePosts(response: String, postSource: PostRepository.PostSource) {
        val listingData = (jsonParser.parse(response) as JSONObject)["data"] as JSONObject?
        val listingPosts = listingData!!["children"] as JSONArray?

        var postOrigin = PostEntity.ORIGIN_SUB
        var listingKey = ""

        when (postSource) {
            is PostRepository.PostSource.Frontpage -> {
                postOrigin = PostEntity.ORIGIN_FRONTPAGE
                listingKey = KEY_FRONTPAGE
            }
            is PostRepository.PostSource.Subreddit -> {
                postOrigin = PostEntity.ORIGIN_SUB
                listingKey = postSource.subredditName
            }
        }

        // create the new listing entity
        val listing = ListingEntity(listingKey, listingData["after"] as String?)

        val postIterator = listingPosts!!.iterator()
        val postEntities = ArrayList<PostEntity>()

        // generate the list of posts using the json array
        while (postIterator.hasNext()) {
            val post = (postIterator.next() as JSONObject)["data"] as JSONObject
            postEntities.add(extractPost(post, postOrigin))
        }

        InsertPostsTask(appDB, postEntities).execute(listing)
    }

    private fun parsePost(response: String, postSource: PostRepository.PostSource) {
        val data = ((jsonParser.parse(response) as JSONArray)[0] as JSONObject)["data"] as JSONObject
        val child = (data["children"] as JSONArray)[0] as JSONObject
        val post = child["data"] as JSONObject

        val postOrigin = when (postSource) {
            is PostRepository.PostSource.Frontpage -> PostEntity.ORIGIN_FRONTPAGE
            is PostRepository.PostSource.Subreddit -> PostEntity.ORIGIN_SUB
            else -> PostEntity.ORIGIN_ALL
        }

        val postEntity = extractPost(post, postOrigin)

        // update the post if it already exists in the db, insert it otherwise
        InsertPostTask().execute(appDB, postEntity)
    }

    /**
     * This is fine for now because I'm still working on finalizing which fields to use/not use
     * There will be a lot more experimentation and changes to come in this method as a result
     */
    @Throws(ParseException::class)
    private fun extractPost(post: JSONObject, postOrigin : Int) : PostEntity {
        // use "api" prefix to indicate fields accessed directly from api
        return gson.fromJson(post.toJSONString(), PostEntity::class.java).apply {
            //Log.d(TAG, "post : " + post.get("title") + " "+ post.get("author"));
            //Log.d(TAG, "src : " + post.get("src") + ", media domain url = "+ post.get("media_domain_url"));
            //Log.d(TAG, "media embed : " + post.get("media_embed") + ", media = "+ post.get("media"));
            //Log.d(TAG, "preview : " + post.get("preview") + " "+ post.get("url"));
            Log.d(TAG, "link_flair_richtext : " + post!!["score"] + " " + post["ups"] + " " + post["wls"] + " " + post["likes"])
            //Log.d(TAG, "link_flair_richtext : " + post.get("visited") + " "+ post.get("views") + " "+ post.get("pwls") + " "+ post.get("gilded"));
            //Log.d(TAG, "post keys " + post.keySet().toString())
            // unmarshall the object and add it into a list

            val apiLikes = post["likes"] as Boolean?
            userUpvoted = if (apiLikes == null) 0 else if (apiLikes) 1 else -1

            if (postOrigin != PostEntity.ORIGIN_SUB) {
                origin = postOrigin
            }

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
        private var postList: List<PostEntity>
    ) : AsyncTask<ListingEntity, Int, Int>() {

        override fun doInBackground(vararg listing: ListingEntity): Int? {
            appDB.postDao.insertPosts(postList)
            appDB.listingDAO.insertListing(listing[0])
            return null
        }
    }

    private class InsertPostTask : AsyncTask<Any, Int, Int>() {
        override fun doInBackground(vararg objects: Any): Int? {
            val applicationDB = objects[0] as ApplicationDB
            applicationDB.postDao.insertPost(objects[1] as PostEntity)
            return null
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
                is PostRepository.PostSource.Frontpage -> appDB.postDao.deleteAllFromSource(ORIGIN_FRONTPAGE)
                is PostRepository.PostSource.All -> appDB.postDao.deleteAllFromSource(ORIGIN_ALL)
                is PostRepository.PostSource.Subreddit -> appDB.postDao.deleteAllFromSub(postSource.subredditName)
            }
        }
    }

    // endregion async tasks
}
