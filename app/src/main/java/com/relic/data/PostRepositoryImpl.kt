package com.relic.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.relic.api.response.Listing
import com.relic.data.deserializer.Contract

import com.relic.data.deserializer.ParsedPostsData
import com.relic.data.entities.PostEntity
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.domain.models.PostModel
import com.relic.data.repository.RepoConstants.ENDPOINT
import com.relic.data.repository.RepoException
import com.relic.domain.models.ListingItem
import com.relic.network.request.RelicRequestError
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.lang.Exception

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
@Reusable
class PostRepositoryImpl @Inject constructor(
    private val requestManager: NetworkRequestManager,
    private val appDB: ApplicationDB,
    private val postDeserializer : Contract.PostDeserializer,
    moshi: Moshi
) : PostRepository {
    private val TAG = "POST_REPO"

    companion object {
        // keys for the "after" value for listings
        private const val KEY_FRONTPAGE = "frontpage"
        private const val KEY_ALL = "all"
        private const val KEY_OTHER = "other"
    }

    private val sortTypesWithScope = arrayOf(
        SortType.HOT,
        SortType.RISING,
        SortType.TOP
    )

    private val type = Types.newParameterizedType(Listing::class.java, ListingItem::class.java)
    private val listingAdapter = moshi.adapter<Listing<ListingItem>>(type)

    // region interface methods

    override fun getPosts(postSource: PostSource) : LiveData<List<PostModel>> {
        return when (postSource) {
            is PostSource.Subreddit -> appDB.postDao.getPostsFromSubreddit(postSource.subredditName)
            is PostSource.Frontpage -> appDB.postDao.postsFromFrontpage
            is PostSource.User -> {
                when (postSource.retrievalOption) {
                    RetrievalOption.Submitted -> appDB.userPostingDao.getUserPosts()
                    RetrievalOption.Comments -> MutableLiveData()
                    RetrievalOption.Saved -> appDB.userPostingDao.getUserSavedPosts()
                    RetrievalOption.Upvoted -> appDB.userPostingDao.getUserUpvotedPosts()
                    RetrievalOption.Downvoted -> appDB.userPostingDao.getUserDownvotedPosts()
                    RetrievalOption.Gilded -> appDB.userPostingDao.getUserGilded()
                    RetrievalOption.Hidden -> appDB.userPostingDao.getUserHidden()
                }
            }
            else -> appDB.postDao.postsFromAll
        }
    }

    override suspend fun getNextPostingVal(callback: RetrieveNextListingCallback, postSource: PostSource) {
        val key = getListingKey(postSource)

        withContext(Dispatchers.IO) {
            // get the "after" value for the most current sub listing
            val subAfter = appDB.listingDAO.getNext(key)
            callback.onNextListing(subAfter)
        }
    }

    override fun getPost(postFullName: String): LiveData<PostModel> {
        return appDB.postDao.getSinglePost(postFullName)
    }

    override suspend fun retrieveUserListing(
        source: PostSource.User,
        sortType: SortType,
        sortScope: SortScope
    ): Listing<out ListingItem> {
        val ending = "user/${source.username}/${source.retrievalOption.name.toLowerCase()}" + when (sortType) {
            SortType.HOT -> "?sort=${sortType.name.toLowerCase()}"
            SortType.TOP, SortType.CONTROVERSIAL -> {
                "?sort=${sortType.name.toLowerCase()}&t=${sortScope.name.toLowerCase()}"
            }
            // default (is "new", no need to manually specify it)
            else -> ""
        }
        Log.d(TAG, "listing items sort $ENDPOINT$ending")

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = "$ENDPOINT$ending"
            )
            Log.d(TAG, "listing items $response")

            return listingAdapter.fromJson(response) ?: throw RepoException.ClientException("retrieve user listing", null)
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve user listing", e) ?: e
        }
    }

    override suspend fun retrieveNextListing(source: PostSource, after: String): Listing<out ListingItem> {
        val ending = when (source) {
            is PostSource.Subreddit -> "r/${source.subredditName}"
            is PostSource.User -> "user/${source.username}/${source.retrievalOption.name.toLowerCase()}"
            else -> ""
        }

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = "$ENDPOINT$ending?after=$after"
            )
            Log.d(TAG, "listing items $response")
            return listingAdapter.fromJson(response) ?: throw RepoException.ClientException("retrieve next listing", null)
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve next listing", e) ?: e
        }
    }

    @Throws(RelicRequestError::class)
    override suspend fun retrieveSortedPosts(
        postSource: PostSource,
        sortType: SortType,
        sortScope: SortScope
    ) {
        // convert into a builder to make it easier to build api url
        // generate the ending of the request url based on the source type
        var ending = ENDPOINT + when (postSource) {
            is PostSource.Subreddit -> "r/${postSource.subredditName}"
            is PostSource.User -> {
                "user/${postSource.username}/${postSource.retrievalOption.name.toLowerCase()}?sort=${sortType.name.toLowerCase()}&t=${sortScope.name.toLowerCase()}"
            }
            else -> ""
        }

        // modify the endpoint based on the sorting options selected by the user
        if (sortType != SortType.DEFAULT && postSource !is PostSource.User) {
            // build the appropriate endpoint based on the "sort by" code and time scope
            ending += "/${sortType.name.toLowerCase()}/"

            // only add sort scope for the options that accept it
            if (sortTypesWithScope.contains(sortType)) ending += "?t=" + sortScope.name.toLowerCase()
        }

        Log.d(TAG, "retrieve sorted posts api url : $ending")
        coroutineScope {
            try {
                val response = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = ending
                )
                val clear = launch { clearAllPostsFromSource(postSource) }
                Log.d(TAG, "retrieve posts response :  $response")

                val listingKey = getListingKey(postSource)
                val parsedData = postDeserializer.parsePosts(response, postSource, listingKey)
                Log.d(TAG, "retrieve more posts : after ${parsedData.listingEntity.afterPosting}")

                clear.join()
                insertParsedPosts(parsedData)
            } catch (e: Exception) {
                throw DomainTransfer.handleException("retrieve sorted posts", e) ?: e
            }
        }
    }

    override suspend fun retrieveMorePosts(
        postSource: PostSource,
        listingAfter: String
    ) {
        // change the api endpoint to access the next post listing
        val ending = when (postSource) {
            is PostSource.Subreddit -> "r/${postSource.subredditName}"
            is PostSource.User -> "user/${postSource.username}/${postSource.retrievalOption.name.toLowerCase()}"
            else -> ""
        }

        Log.d(TAG, "retrieve more posts : api url : $ENDPOINT$ending?after=$listingAfter")
        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = "$ENDPOINT$ending?after=$listingAfter"
            )
            Log.d(TAG, "more posts $response")

            val listingKey = getListingKey(postSource)
            val parsedData = postDeserializer.parsePosts(response, postSource, listingKey)

            insertParsedPosts(parsedData)
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve more posts", e) ?: e
        }
    }

    override suspend fun retrievePost(
        subredditName: String,
        postFullName: String,
        postSource: PostSource
    ) {
        val ending = "r/$subredditName/comments/${postFullName.substring(3)}"

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = ENDPOINT + ending
            )

            postDeserializer.parsePost(response).apply {
                postEntity.visited = true

                withContext(Dispatchers.IO) {
                    appDB.postDao.insertPost(postEntity)
                    appDB.postSourceDao.insertPostSources(listOf(postSourceEntity))
                }
            }

        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve account", e) ?: e
        }
    }

    override suspend fun clearAllPostsFromSource(postSource: PostSource) {
        withContext(Dispatchers.IO) {
            when (postSource) {
                is PostSource.Frontpage -> appDB.postSourceDao.removeAllFrontpageAsSource()
                is PostSource.All -> appDB.postSourceDao.removeAllAllAsSource()
                is PostSource.Subreddit -> appDB.postSourceDao.removeAllSubredditAsSource(postSource.subredditName)
                is PostSource.User -> {
                    appDB.postSourceDao.apply {
                        when (postSource.retrievalOption) {
                            RetrievalOption.Submitted -> removeAllUserSubmittedAsSource()
                            RetrievalOption.Comments -> removeAllUserCommentsAsSource()
                            RetrievalOption.Saved -> removeAllUserSavedAsSource()
                            RetrievalOption.Upvoted -> removeAllUserUpvotedAsSource()
                            RetrievalOption.Downvoted -> removeAllUserDownvotedAsSource()
                            RetrievalOption.Gilded -> removeAllUserGildedAsSource()
                            RetrievalOption.Hidden -> removeAllUserHiddenAsSource()
                        }
                    }
                }
            }

            // remove all the source entities that no longer correspond to any remaining posts
            appDB.postSourceDao.removeAllUnusedSources()
        }
    }

    override suspend fun searchSubPosts(
        subredditName: String,
        query : String,
        restrictToSub : Boolean,
        after : String?
    ) : SubSearchResult {
        var ending = "r/$subredditName/search?q=$query"
        if (restrictToSub) ending += "&restrict_sr=true"
        if (after != null) ending += "&after=$after"

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = ENDPOINT + ending
            )
            Log.d(TAG, "ending $ending")
            Log.d(TAG, "response $response")
            return postDeserializer.parseSearchSubPostsResponse(response)

        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve search results", e) ?: e
        }
    }

    override suspend fun postPost(postDraft: PostDraft, type : PostType) {
        // based on the post type,
        val url = "${ENDPOINT}api/submit"

        val data = HashMap<String, String>().apply {
            put("title", postDraft.title)
            put("sr", postDraft.subreddit)
            put("nsfw", postDraft.nsfw.toString())
            put("spoiler", postDraft.spoiler.toString())
            put("resubmit", postDraft.resubmit.toString())
            put("send_replies", postDraft.sendReplies.toString())

            when (type) {
                is PostType.Self -> {
                    put("kind","self")
                    put("text", postDraft.body!!)
                }
                is PostType.Link -> {
                    put("kind","link")
                }
            }
        }

        Log.d(TAG, "post post draft ${postDraft.sendReplies}  ${type}")

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.POST,
                url = url,
                data = data
            )

            Log.d(TAG, "post post $response")
            // delete the post draft when we've successfully submitted it
            appDB.postDao.deletePostDraft(postDraft.subreddit)

        } catch (e: Exception) {
            throw DomainTransfer.handleException("post post", e) ?: e
        }
    }

    override suspend fun saveDraft(postDraft: PostDraft) {
        withContext(Dispatchers.IO) {
            val newPostDraft = PostEntity().apply {
                name = ""
                author = ""
                title = postDraft.title
                selftext = postDraft.body
                subreddit = postDraft.subreddit
            }

            appDB.postDao.insertPost(newPostDraft)
        }
    }

    override suspend fun loadDraft(subreddit : String) : PostDraft? {
        return withContext(Dispatchers.IO) {
            val draftModel = appDB.postDao.getPostDraft(subreddit)

            if (draftModel != null) {
                PostDraft(
                    title = draftModel.title,
                    body = draftModel.selftext,
                    subreddit = draftModel.subreddit!!
                )
            } else null
        }
    }

    // endregion interface methods


    private suspend fun insertParsedPosts(parsedPosts : ParsedPostsData) {
        withContext(Dispatchers.IO) {
            parsedPosts.apply {
                if (postEntities.isNotEmpty()) appDB.postDao.insertPosts(postEntities)
                if (commentEntities.isNotEmpty()) appDB.commentDAO.insertComments(commentEntities)

                appDB.postSourceDao.insertPostSources(postSourceEntities)
                appDB.listingDAO.insertListing(listingEntity)
            }
        }
    }

    /**
     * builds the key used for retrieving the "after" value for a listing using its associated
     * post source
     */
    private fun getListingKey(postSource : PostSource) : String {
        return when (postSource) {
            is PostSource.Subreddit -> postSource.subredditName
            is PostSource.Frontpage -> KEY_FRONTPAGE
            is PostSource.All -> KEY_ALL
            is PostSource.User -> postSource.username + postSource.retrievalOption.name
            else -> KEY_OTHER
        }
    }
}
