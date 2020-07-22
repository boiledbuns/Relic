package com.relic.data

import android.os.Parcelable
import androidx.lifecycle.LiveData
import com.relic.api.response.Listing
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.network.request.RelicRequestError
import kotlinx.android.parcel.Parcelize

interface PostRepository {
    /**
     * Exposes the livedata list of posts
     * @param postSource subreddit to get the list for
     * @return live data list of posts
     */
    fun getPosts(postSource: PostSource): LiveData<List<PostModel>>

    /**
     * clears all current posts for this subreddit and retrieves new ones based on the sorting
     * method specified
     * @param sortType
     * @param sortScope
     */
    @Throws(RelicRequestError::class)
    suspend fun retrieveSortedPosts(postSource: PostSource, sortType: SortType, sortScope: SortScope) : Listing<PostModel>

    /**
     * Retrieves posts for a subreddit
     * @param postSource origin of the post
     * @param listingAfter after value associated with the listing of the current set of posts
     */
    suspend fun retrieveMorePosts(postSource: PostSource, listingAfter: String) : Listing<PostModel>

    /**
     * this method is used specifically to retrieve listings that can have both post and comments.
     * currently, this is only used when displaying users.
     */
    suspend fun retrieveUserListing(
        source: PostSource.User,
        sortType: SortType,
        sortScope: SortScope
    ) : Listing<out ListingItem>

    suspend fun retrieveNextListing(
        source: PostSource,
        after : String
    ) : Listing<out ListingItem>

    /**
     * exposes a single post model as livedata
     * @param postFullName a valid "full name" for a post
     * @return a model of the post wrapped in Livedata
     */
    fun getPost(postFullName: String): LiveData<PostModel>

    /**
     * retrieves a single post from the network and stores it locally
     * @param subredditName name of subreddit that the post was made in
     * @param postFullName "full name" of the subreddit
     */
    suspend fun retrievePost(
        subredditName: String,
        postFullName: String,
        postSource: PostSource
    )

    suspend fun insertPosts (source : PostSource, posts : List<PostModel>)

    suspend fun searchSubPosts(
        subredditName: String,
        query : String,
        restrictToSub : Boolean = false,
        after : String? = null
    ) : Listing<PostModel>

    suspend fun searchOfflinePosts(
        sourceRestriction: PostSource?,
        query : String
    ) : List<PostModel>

    suspend fun postPost(postDraft: PostDraft, type : PostType)

    suspend fun saveDraft(postDraft: PostDraft)

    suspend fun loadDraft(subreddit : String) : PostDraft?

    /**
     * //TODO tentative -> should expose or not
     * need to decide whether the viewModel should handle this or not
     * @param postSource
     */
    suspend fun clearAllPostsFromSource(postSource: PostSource)
}

data class SubSearchResult(
    val posts : List<PostModel>,
    val after : String?
)

enum class SortType {
    DEFAULT, BEST, CONTROVERSIAL, HOT, NEW, RISING, TOP
}

enum class SortScope {
    NONE, HOUR, DAY, WEEK, MONTH, YEAR, ALL
}

/**
 * Used to represent how a post is being accessed
 * Eg. Accessing a post from the Frontpage uses the "Frontpage" source
 */
sealed class PostSource : Parcelable {

    @Parcelize
    object Frontpage : PostSource() {
        override fun getSourceName() = "frontpage"
    }

    @Parcelize
    object All : PostSource() {
        override fun getSourceName() = "all"
    }

    @Parcelize
    object Popular : PostSource() {
        override fun getSourceName() = "popular"
    }

    @Parcelize
    data class Subreddit(
        val subredditName : String
    ) : PostSource() {
        override fun getSourceName() = subredditName
    }

    @Parcelize
    data class User(
        val username : String,
        val retrievalOption: RetrievalOption
    ) : PostSource() {
        override fun getSourceName() = username + "_" + retrievalOption
    }

    @Parcelize
    data class Post(
        val postFullname : String
    ) : PostSource() {
        override fun getSourceName() = postFullname
    }

    abstract fun getSourceName() : String
}

enum class RetrievalOption {
    Submitted, Comments,
    // these should only be available for the current user
    Saved, Upvoted, Downvoted, Gilded, Hidden
}

data class PostDraft(
    val title : String,
    val body : String?,
    val subreddit : String,
    val nsfw : Boolean = false,
    val spoiler : Boolean = false,
    val resubmit : Boolean = false,
    val sendReplies : Boolean = true
)

sealed class PostType {
    class Self : PostType()
    class Link : PostType()
}
