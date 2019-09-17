package com.relic.data

import androidx.lifecycle.LiveData
import com.relic.api.response.Listing
import com.relic.data.deserializer.Contract
import com.relic.data.repository.RepoConstants.ENDPOINT
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.relic.network.request.RelicRequestError
import com.relic.persistence.ApplicationDB
import com.relic.persistence.entities.SourceAndPostRelation
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
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
    private val postDeserializer : Contract.PostDeserializer
) : PostRepository {

    private val sortTypesWithScope = arrayOf(
        SortType.HOT,
        SortType.RISING,
        SortType.TOP
    )

    private val postDao = appDB.postDao
    private val postSourceDao = appDB.postSourceDao

    // region interface methods

    override fun getPosts(postSource: PostSource) : LiveData<List<PostModel>> {
        return postDao.getPostsFromSource(postSource.getSourceName())
    }

    override fun getPost(postFullName: String): LiveData<PostModel> {
        return postDao.getSinglePost(postFullName)
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
        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = "$ENDPOINT$ending"
            )

            return postDeserializer.parseListingItems(response)
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
            return postDeserializer.parseListingItems(response)
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve next listing", e) ?: e
        }
    }

    @Throws(RelicRequestError::class)
    override suspend fun retrieveSortedPosts(
        postSource: PostSource,
        sortType: SortType,
        sortScope: SortScope
    ) : Listing<PostModel> {
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

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = ending
            )

            val listing = postDeserializer.parsePosts(response)
            Timber.d( "retrieve more posts : after ${listing.data.after}")

            return listing.apply {
                data.children?.let { children -> convergeVisited(children) }
            }
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve sorted posts", e) ?: e
        }
    }

    override suspend fun retrieveMorePosts(
        postSource: PostSource,
        listingAfter: String
    ) : Listing<PostModel> {
        // change the api endpoint to access the next post listing
        val ending = when (postSource) {
            is PostSource.Subreddit -> "r/${postSource.subredditName}"
            is PostSource.User -> "user/${postSource.username}/${postSource.retrievalOption.name.toLowerCase()}"
            else -> ""
        }

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = "$ENDPOINT$ending?after=$listingAfter"
            )

            return postDeserializer.parsePosts(response).apply {
                data.children?.let { children -> convergeVisited(children) }
            }
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve more posts", e) ?: e
        }
    }

    private suspend fun convergeVisited(posts : List<PostModel>) {
        // map fullname to position in list
        val positionMap = HashMap<String, Int>()
        var position = 0

        val postNames = posts.map {
            it.fullName.apply {
                positionMap[this] = position
                position ++
            }
        }

        val visited = withContext(Dispatchers.IO){
            appDB.postVisitedDao.getVisited(postNames)
        }

        for (visitedPost in visited) {
            val postPos = positionMap[visitedPost]!!
            posts[postPos].visited = true
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
                visited = true
                val postSourceRelation = SourceAndPostRelation(source = id, postId = id, position = 0)

                withContext(Dispatchers.IO) {
                    postDao.insertPost(this@apply)
                    postSourceDao.insertPostSourceRelations(listOf(postSourceRelation))
                }
            }

        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve account", e) ?: e
        }
    }

    override suspend fun clearAllPostsFromSource(postSource: PostSource) {
        val sourceName = postSource.getSourceName()
        withContext(Dispatchers.IO) {
            postSourceDao.removeAllFromSource(sourceName)
            // TODO decide if we need this later on. Not sure if we do
//            if (postSourceDao.getItemsCountForSource(sourceName) == 0) {
//                postDao.deletePostsWithoutSources()
//            }
        }
    }

    override suspend fun searchSubPosts(
        subredditName: String,
        query : String,
        restrictToSub : Boolean,
        after : String?
    ) : Listing<PostModel> {
        var ending = "r/$subredditName/search?q=$query"
        if (restrictToSub) ending += "&restrict_sr=true"
        if (after != null) ending += "&after=$after"

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = ENDPOINT + ending
            )
            return postDeserializer.parsePosts(response)

        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve search results", e) ?: e
        }
    }

    override suspend fun searchOfflinePosts(subredditName: String, query: String, restrictToSub: Boolean): List<PostModel> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.POST,
                url = url,
                data = data
            )

            // delete the post draft when we've successfully submitted it
            postDao.deletePostDraft(postDraft.subreddit)

        } catch (e: Exception) {
            throw DomainTransfer.handleException("post post", e) ?: e
        }
    }

    override suspend fun saveDraft(postDraft: PostDraft) {
        withContext(Dispatchers.IO) {
            val newPostDraft = PostModel().apply {
                fullName = ""
                author = ""
                title = postDraft.title
                selftext = postDraft.body
                subreddit = postDraft.subreddit
            }

            postDao.insertPost(newPostDraft)
        }
    }

    override suspend fun loadDraft(subreddit : String) : PostDraft? {
        return withContext(Dispatchers.IO) {
            val draftModel = postDao.getPostDraft(subreddit)

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

    override suspend fun insertPosts (source : PostSource, posts : List<PostModel>) {
        val sourceName = source.getSourceName()

        withContext(Dispatchers.IO) {
            var position = appDB.postSourceDao.getItemsCountForSource(sourceName)

            val sourceRelations = posts.map {
                position ++
                SourceAndPostRelation(source = sourceName, postId = it.id, position = position)
            }

            postDao.insertPosts(posts)
            postSourceDao.insertPostSourceRelations(sourceRelations)
            // TODO also need to insert listing "after" but maybe separately
        }
    }
}
