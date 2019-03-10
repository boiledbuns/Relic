package com.relic.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.volley.NoConnectionError

import com.relic.R
import com.relic.data.deserializer.ParsedPostsData
import com.relic.data.deserializer.PostDeserializerImpl
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

import org.json.simple.parser.ParseException
import java.lang.Exception

import java.util.ArrayList

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

    private val appDB: ApplicationDB = ApplicationDB.getDatabase(appContext)
    // TODO convert this to DI
    private val postDeserializer = PostDeserializerImpl(appContext)

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
                    PostRepository.RetrievalOption.Submitted -> appDB.userPostingDao.getUserPosts()
                    PostRepository.RetrievalOption.Comments -> MutableLiveData()
                    PostRepository.RetrievalOption.Saved -> appDB.userPostingDao.getUserSavedPosts()
                    PostRepository.RetrievalOption.Upvoted -> appDB.userPostingDao.getUserUpvotedPosts()
                    PostRepository.RetrievalOption.Downvoted -> appDB.userPostingDao.getUserDownvotedPosts()
                    PostRepository.RetrievalOption.Gilded -> appDB.userPostingDao.getUserGilded()
                    PostRepository.RetrievalOption.Hidden -> appDB.userPostingDao.getUserHidden()
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
            is PostRepository.PostSource.User -> "user/${postSource.username}/${postSource.retrievalOption.name.toLowerCase()}"
            else -> ""
        }
        coroutineScope {
            Log.d(TAG, "retrieve more posts : api url : $ENDPOINT$ending?after=$listingAfter")

            try {
                // create the new request and submit it
                val response = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = "$ENDPOINT$ending?after=$listingAfter",
                    authToken = checkToken()
                )
                Log.d(TAG, "more posts $response")

                val listingKey = getListingKey(postSource)
                val parsedData = postDeserializer.parsePosts(response, postSource, listingKey)
                launch { insertParsedPosts(parsedData) }

            } catch (e: Exception) {
                Log.d(TAG, "Error: " + e.message)
            }
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
        // convert into a builder to make it easier to build api url
        // generate the ending of the request url based on the source type
        var ending = ENDPOINT + when (postSource) {
            is PostRepository.PostSource.Subreddit -> "r/${postSource.subredditName}"
            is PostRepository.PostSource.User -> {
                "user/${postSource.username}/${postSource.retrievalOption.name.toLowerCase()}?sort=${sortType.name.toLowerCase()}&t=${sortScope.name.toLowerCase()}"
            }
            else -> ""
        }

        // modify the endpoint based on the sorting options selected by the user
        if (sortType != PostRepository.SortType.DEFAULT && postSource !is PostRepository.PostSource.User) {
            // build the appropriate endpoint based on the "sort by" code and time scope
            ending += "/${sortType.name.toLowerCase()}/"

            // only add sort scope for the options that accept it
            if (sortTypesWithScope.contains(sortType)) ending += "?t=" + sortScope.name.toLowerCase()
        }

        coroutineScope {
            Log.d(TAG, "retrieve sorted posts api url : $ending")
            try {
                val response = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = ending,
                    authToken = checkToken()
                )
                Log.d(TAG, "retrieve posts response :  $response")

                val listingKey = getListingKey(postSource)
                val parsedData = postDeserializer.parsePosts(response, postSource, listingKey)
                Log.d(TAG, "retrieve more posts : after ${parsedData.listingEntity.afterPosting}")
                launch { insertParsedPosts(parsedData) }

            } catch (e: Exception) {
                when (e) {
                    is ParseException -> Log.d(TAG, "Error parsing sorted posts $e")
                    else -> Log.d(TAG, "Error retrieving sorted posts : $e")
                }

            }
        }
    }

    override suspend fun retrievePost(
        subredditName: String,
        postFullName: String,
        postSource: PostRepository.PostSource,
        errorHandler: (error : RelicRequestError) -> Unit
    ) {
        val ending = "r/$subredditName/comments/${postFullName.substring(3)}"

        coroutineScope {
            try {
                val response = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = ENDPOINT + ending,
                    authToken = checkToken()
                )

                postDeserializer.parsePost(response).apply {
                    postEntity.visited = true

                    launch {
                        appDB.postDao.insertPost(postEntity)
                        appDB.postSourceDao.insertPostSources(listOf(postSourceEntity))
                    }
                }

            } catch (e: Exception) {
                // TODO decide if it would be better to move this to another method
                when (e) {
                    is NoConnectionError -> {
                        errorHandler.invoke(RelicRequestError.NetworkUnavailableError())
                    }
                    else -> Log.d(TAG, "Error retrieving post: $e")
                }
            }
        }
    }

    override suspend fun clearAllPostsFromSource(postSource: PostRepository.PostSource) {
        ClearPostsFromSourceTask().execute(appDB, postSource)
    }

    // endregion interface methods

    // region helper functions

    private fun insertParsedPosts(parsedPosts : ParsedPostsData) {
        parsedPosts.apply {
            if (postEntities.isNotEmpty()) appDB.postDao.insertPosts(postEntities)
            if (commentEntities.isNotEmpty()) appDB.commentDAO.insertComments(commentEntities)

            appDB.postSourceDao.insertPostSources(postSourceEntities)
            appDB.listingDAO.insertListing(listingEntity)
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
                    appDB.postSourceDao.apply {
                        when (postSource.retrievalOption) {
                            PostRepository.RetrievalOption.Submitted -> removeAllUserSubmittedAsSource()
                            PostRepository.RetrievalOption.Comments -> removeAllUserCommentsAsSource()
                            PostRepository.RetrievalOption.Saved -> removeAllUserSavedAsSource()
                            PostRepository.RetrievalOption.Upvoted -> removeAllUserUpvotedAsSource()
                            PostRepository.RetrievalOption.Downvoted -> removeAllUserDownvotedAsSource()
                            PostRepository.RetrievalOption.Gilded -> removeAllUserGildedAsSource()
                            PostRepository.RetrievalOption.Hidden -> removeAllUserHiddenAsSource()
                        }
                    }
                }
            }
            // remove all the source entities that no longer correspond to any remaining posts
            appDB.postSourceDao.removeAllUnusedSources()
        }
    }

    // endregion async tasks
}
