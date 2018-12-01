package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.text.Html
import android.util.Log
import com.android.volley.Response

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.relic.R
import com.relic.network.NetworkRequestManager
import com.relic.data.gateway.PostGateway
import com.relic.data.gateway.PostGatewayImpl
import com.relic.network.request.RelicOAuthRequest
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.data.entities.ListingEntity
import com.relic.data.entities.PostEntity
import com.relic.data.models.PostModel

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

// TODO convert to KOTLIN
class PostRepositoryImpl @Inject
constructor(
    private val currentContext: Context,
    private val requestManager: NetworkRequestManager
) : PostRepository {
    private val ENDPOINT = "https://oauth.reddit.com/"
    private val userAgent = "android:com.relic.Relic (by /u/boiledbuns)"
    private val TAG = "POST_REPO"

    private val sortByMethods = arrayOf("best", "controversial", "hot", "new", "rising", "top")
    private val sortScopes = arrayOf("hour", "day", "week", "month", "year", "all")
    private val JSONParser: JSONParser = JSONParser()
    private val appDB: ApplicationDB = ApplicationDB.getDatabase(currentContext)

    override val postGateway: PostGateway
        get() = PostGatewayImpl(currentContext, requestManager)

    // get the oauth token from the app's shared preferences
    private fun checkToken(): String {
        // retrieve the auth token shared preferences
        val authKey = currentContext.resources.getString(R.string.AUTH_PREF)
        val tokenKey = currentContext.resources.getString(R.string.TOKEN_KEY)
        return currentContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
            .getString(tokenKey, "DEFAULT")
    }

    /**
     * Exposes the livedata list of posts
     * @param subreddit subreddit to get the list for
     * @return livedata list of posts
     */
    override fun getPosts(subreddit: String): LiveData<List<PostModel>> {
        val subredditPosts: LiveData<List<PostModel>>
        // handles specific cases
        // try to convert this to enum if time permits
        if (subreddit.isEmpty()) {
            subredditPosts = appDB.postDao.frontPagePosts
        } else {
            subredditPosts = appDB.postDao.getSubredditPosts(subreddit)
        }
        return subredditPosts
    }

    /**
     * Retrieves posts for a subreddit
     * @param subredditName name of the subreddit
     * @param after the full name of the page to retrieve the posts from
     */
    override fun retrieveMorePosts(postSource: PostRepository.PostSource, after: String) {
        // change the api endpoint to access to get the next post listing
        var ending = ENDPOINT + when (postSource) {
            is PostRepository.PostSource.Subreddit -> "r/${postSource.subredditName}?after=$after"
            else -> ""
        }

        // create the new request and submit it
        requestManager.processRequest(
            RelicOAuthRequest(
                RelicOAuthRequest.GET,
                ENDPOINT + ending,
                Response.Listener { response ->
                    try {
                        parsePosts(response, postSource)
                        //new InsertPostsTask(appDB, parsePosts(response, subredditName)).execute();
                    } catch (error: ParseException) {
                        Log.d(TAG, "Error: " + error.message)
                    }
                },
                Response.ErrorListener { error -> Log.d(TAG, "Error: " + error.message) },
                checkToken()
            )
        )
    }

    override fun retrieveSortedPosts(postSource : PostRepository.PostSource, sortType: Int) {
        retrieveSortedPosts(postSource, sortType, 0)
    }

    /**
     * Deletes all locally stored posts and retrieves a new set based on the sorting method specified
     * by the caller
     * @param subredditName name of the subreddit for the posts to be retrieved
     * @param sortType code for the associated sort by method
     * @param sortScope  code for the associate time span to sort by
     */
    override fun retrieveSortedPosts(postSource: PostRepository.PostSource, sortType: Int, sortScope: Int) {

        // generate the ending of the request url based on the source type
        var ending = ENDPOINT + when (postSource) {
            is PostRepository.PostSource.Subreddit -> "r/" + postSource.subredditName
            else -> ""
        }

        // change the endpoint based on which sorting option the user has selected
        if (sortType != PostRepository.SORT_DEFAULT && sortType <= sortByMethods.size) {
            // build the appropriate endpoint based on the "sort by" code and time scope
            ending += "/" + sortByMethods[sortType - 1] + "/?sort=" + sortByMethods[sortType - 1]

            // only add sort scope for these sorting types
            if (sortType == PostRepository.SORT_HOT || sortType == PostRepository.SORT_RISING || sortType == PostRepository.SORT_TOP) {
                // add the scope only if the sorting type has one
                ending += "&t=" + sortScopes[sortScope - 1]
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

    /**
     * Parses the response from the api and stores the posts in the persistence layer
     * @param response the json response from the server with the listing object
     * @throws ParseException
     */
    @Throws(ParseException::class)
    private fun parsePosts(response: String, postSource: PostRepository.PostSource): List<PostEntity> {
        //TODO separate into two separate methods and switch to multithreaded to avoid locking main thread
        val listingData = (JSONParser.parse(response) as JSONObject)["data"] as JSONObject?
        val listingPosts = listingData!!["children"] as JSONArray?

        // initialize the date formatter and date for now
        val formatter = SimpleDateFormat("MMM dd',' hh:mm a")
        val current = Date()

        var origin = PostEntity.ORIGIN_SUB
        val listingKey = when (postSource) {
            is PostRepository.PostSource.Subreddit -> postSource.subredditName
            else -> {
                origin = PostEntity.ORIGIN_FRONTPAGE
                "frontpage"
            }
        }

        // create the new listing entity
        val listing = ListingEntity(listingKey, listingData!!["after"] as String?)
        Log.d(TAG, "Listing after val : " + listing.afterPosting)

        // GSON reader to unmarshall the json response
        val gson = GsonBuilder().create()

        val postIterator = listingPosts!!.iterator()
        val postEntities = ArrayList<PostEntity>()

        // generate the list of posts using the json array
        while (postIterator.hasNext()) {
            val post = (postIterator.next() as JSONObject)["data"] as JSONObject?
            //Log.d(TAG, "post : " + post.get("title") + " "+ post.get("author"));
            //Log.d(TAG, "src : " + post.get("src") + ", media domain url = "+ post.get("media_domain_url"));
            //Log.d(TAG, "media embed : " + post.get("media_embed") + ", media = "+ post.get("media"));
            //Log.d(TAG, "preview : " + post.get("preview") + " "+ post.get("url"));
            Log.d(
                TAG,
                "link_flair_richtext : " + post!!["score"] + " " + post["ups"] + " " + post["wls"] + " " + post["likes"]
            )
            //Log.d(TAG, "link_flair_richtext : " + post.get("visited") + " "+ post.get("views") + " "+ post.get("pwls") + " "+ post.get("gilded"));

            //Log.d(TAG, "post keys " + post.keySet().toString())
            // unmarshall the object and add it into a list
            val postEntity = gson.fromJson(post.toJSONString(), PostEntity::class.java)
            val likes = post["likes"] as Boolean?
            postEntity.userUpvoted = if (likes == null) 0 else if (likes) 1 else -1

            if (origin != PostEntity.ORIGIN_SUB) {
                postEntity.origin = PostEntity.ORIGIN_FRONTPAGE
            }


            // TODO create parse class/switch to a more efficient method of removing html
            val authorFlair = post["author_flair_text"] as String?
            if (authorFlair != null && !authorFlair.isEmpty()) {
                postEntity.author_flair_text = Html.fromHtml(authorFlair).toString()
            } else {
                postEntity.author_flair_text = null
            }
            Log.d(TAG, "epoch = " + post["created"]!!)

            // add year to stamp if the post year doesn't match the current one
            val created = Date((post["created"] as Double).toLong() * 1000)
            if (current.year != created.year) {
                postEntity.created = created.year.toString() + " " + formatter.format(created)
            } else {
                postEntity.created = formatter.format(created)
            }

            postEntities.add(postEntity)
        }

        // insert all the post entities into the db
        InsertPostsTask(appDB, postEntities).execute(listing)

        return postEntities
    }

    /**
     * Async task to insert posts and create/update the listing data for the current subreddit to
     * point to the next listing
     */
    internal class InsertPostsTask(var appDB: ApplicationDB, var postList: List<PostEntity>) :
        AsyncTask<ListingEntity, Int, Int>() {

        override fun doInBackground(vararg listing: ListingEntity): Int? {
            appDB.postDao.insertPosts(postList)
            appDB.listingDAO.insertListing(listing[0])
            return null
        }
    }

    /**
     * Retrieves the "after" values to be used for the next post listing
     * @param callback callback to send the name to
     * @param subName name of the sub to retrieve the "after" value for
     */
    override fun getNextPostingVal(callback: RetrieveNextListingCallback, subName: String) {
        RetrieveListingAfterTask(appDB, callback).execute(subName)
    }

    internal class RetrieveListingAfterTask(
        var appDB: ApplicationDB,
        var callback: RetrieveNextListingCallback
    ) : AsyncTask<String, Int, Int>() {

        override fun doInBackground(vararg strings: String): Int? {
            // get the "after" value for the most current sub listing
            val subAfter = appDB.listingDAO.getNext(strings[0])
            callback.onNextListing(subAfter)
            return null
        }
    }

    internal class DeleteSubPostsTask(var appDB: ApplicationDB) : AsyncTask<String, Int, Int>() {
        var callback: RetrieveNextListingCallback? = null

        override fun doInBackground(vararg strings: String): Int? {
            // delete the posts associated with the current sub
            appDB.postDao.deleteAllFromSub(strings[0])
            return null
        }
    }

    override fun getPost(postFullName: String): LiveData<PostModel> {
        return appDB.postDao.getSinglePost(postFullName)
    }

    override fun retrievePost(subredditName: String, postFullName: String) {
        val ending = ENDPOINT + "r/" + subredditName + "/comments/" + postFullName.substring(3)

        Log.d(TAG, ending)

        // create the new request and submit it
        requestManager.processRequest(
            RelicOAuthRequest(
                RelicOAuthRequest.GET,
                ending,
                Response.Listener { response ->
                    Log.d(TAG, "Loaded response $response")
                    try {
                        val post = parsePost(response)
                        InsertPostTask().execute(appDB, post)
                    } catch (error: ParseException) {
                        Log.d(TAG, "Error: " + error.message)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d(TAG, "Error: " + error.networkResponse)
                    // TODO add livedata for error
                    // TODO maybe retry if not an internet connection issue
                },
                checkToken()
            )
        )
    }

    @Throws(ParseException::class)
    private fun parsePost(response: String): PostEntity {
        val gson = GsonBuilder().create()
        val data =
            ((JSONParser.parse(response) as JSONArray)[0] as JSONObject)["data"] as JSONObject?
        val child = (data!!["children"] as JSONArray)[0] as JSONObject
        val post = child["data"] as JSONObject?

        return gson.fromJson(post!!.toJSONString(), PostEntity::class.java)
    }

    private class InsertPostTask : AsyncTask<Any, Int, Int>() {
        override fun doInBackground(vararg objects: Any): Int? {
            val applicationDB = objects[0] as ApplicationDB
            applicationDB.postDao.insertPost(objects[1] as PostEntity)
            return null
        }
    }

    override fun clearAllSubPosts(subredditName: String) {
        ClearSubredditPosts().execute(appDB, subredditName)
    }

    private class ClearSubredditPosts : AsyncTask<Any, Int, Int>() {
        override fun doInBackground(vararg objects: Any): Int? {
            val appDB = objects[0] as ApplicationDB
            appDB.postDao.deleteAllFromSub(objects[1] as String)

            return null
        }
    }
}
