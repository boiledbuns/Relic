package com.relic.presentation.displayuser

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.UserRepository
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.displaysub.SubNavigationData
import com.relic.presentation.helper.ImageHelper
import com.relic.presentation.util.RelicEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class DisplayUserVM(
    private val postRepo: PostRepository,
    private val commentRepo: CommentRepository,
    private val listingRepo: ListingRepository,
    private val userRepo: UserRepository,
    private val postGateway: PostGateway,
    private val username : String
) : RelicViewModel(), DisplayUserContract.ListingItemAdapterDelegate {

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val commentRepo: CommentRepository,
        private val listingRepo: ListingRepository,
        private val userRepo: UserRepository,
        private val postGateway: PostGateway
    ) {
        fun create(username : String) : DisplayUserVM {
            return DisplayUserVM(postRepo, commentRepo, listingRepo, userRepo, postGateway, username)
        }
    }

    private var _userLiveData = MutableLiveData<UserModel>()
    var userLiveData : LiveData<UserModel> = _userLiveData

    private var _navigationLiveData = MutableLiveData<RelicEvent<SubNavigationData>>()
    var navigationLiveData : LiveData<RelicEvent<SubNavigationData>> = _navigationLiveData

    // dictionary mapping a tab to its associated livedata
    // items should only be added if the tab is being accessed for the first time
    private var postsLiveData = mutableMapOf<UserTab, MediatorLiveData<List<ListingItem>>>()

    private var currentSortingType = mutableMapOf<UserTab, PostRepository.SortType>()
    private var currentSortingScope = mutableMapOf<UserTab, PostRepository.SortScope>()
    private var currentAfterValues = mutableMapOf<UserTab, String?>()

    private lateinit var currentTab : UserTab

    init {
        launch(Dispatchers.Main) {
            _userLiveData.postValue(userRepo.retrieveUser(username))
        }
    }

    fun getTabPostsLiveData(tab : UserTab) : LiveData<List<ListingItem>> {
        if (postsLiveData[tab] == null) {
            // create a new livedata if it doesn't already exist
            postsLiveData[tab] = MediatorLiveData()

            // request new posts if this is the first time the tab is being created
            // TODO need to separate the process of deleting old posts from retrieving new posts
            // TODO to prevent the old posts being deleted --> internet connection unavailable --> so new posts
            requestPosts(tab = tab, refresh = true)
        }
        return postsLiveData[tab]!!
    }

    fun requestPosts(tab : UserTab, refresh : Boolean) {
        // subscribe to the appropriate livedata based on tab selected
        val userRetrievalOption = toRetrievalOption(tab)
        val postSource = PostRepository.PostSource.User(username, userRetrievalOption)

        val type = currentSortingType[tab] ?: PostRepository.SortType.DEFAULT
        val scope = currentSortingScope[tab] ?: PostRepository.SortScope.NONE

        launch(Dispatchers.Main) {
            val listing = if (refresh) {
                postsLiveData[tab]!!.postValue(emptyList())
                postGateway.retrieveListingItems(postSource)
//                runBlocking { postRepo.clearAllPostsFromSource(postSource) }
//                postRepo.retrieveSortedPosts(postSource, type, scope)
            } else {
                postGateway.retrieveListingItems(postSource, listingAfter = currentAfterValues[tab])
            }

            Log.d(TAG, "listing after ${listing.data.after}")
            Log.d(TAG, "listing after tab ${currentAfterValues[tab]}")
            listing.data.children?.let { items ->
                val currentList = postsLiveData[tab]!!.value ?: emptyList()
                postsLiveData[tab]!!.postValue(currentList.plus(items))

                // only update the "after" val for this tab if successful
                currentAfterValues[tab] = listing.data.after
            }

            // TODO based on user preferences -> save data offline
        }
    }

    fun setCurrentTab(tab : UserTab) {
        currentTab = tab
    }

    fun changeSortingMethod(sortType: PostRepository.SortType? = null, sortScope: PostRepository.SortScope? = null) {
        sortType?.let { currentSortingType[currentTab] = it }
        sortScope?.let { currentSortingScope[currentTab] = it }

        when (currentSortingType[currentTab]) {
            // these sorting types don't have a scope, so retrieve sorted scopes asap
            PostRepository.SortType.NEW, PostRepository.SortType.BEST, PostRepository.SortType.CONTROVERSIAL -> {
                requestPosts(currentTab, refresh = true)
            }
        }

        when (currentSortingScope[currentTab]) {
            // when the sorting scope is changed
            PostRepository.SortScope.HOUR, PostRepository.SortScope.DAY, PostRepository.SortScope.WEEK,
            PostRepository.SortScope.MONTH, PostRepository.SortScope.YEAR, PostRepository.SortScope.ALL -> {
                requestPosts(currentTab, refresh = true)
            }
        }
    }

    // region helper functions

    private fun toRetrievalOption(tab : UserTab): PostRepository.RetrievalOption {
        return when (tab) {
            is UserTab.Submitted -> PostRepository.RetrievalOption.Submitted
            is UserTab.Comments -> PostRepository.RetrievalOption.Comments
            is UserTab.Saved -> PostRepository.RetrievalOption.Saved
            is UserTab.Upvoted -> PostRepository.RetrievalOption.Upvoted
            is UserTab.Downvoted -> PostRepository.RetrievalOption.Downvoted
            is UserTab.Gilded -> PostRepository.RetrievalOption.Gilded
            is UserTab.Hidden -> PostRepository.RetrievalOption.Hidden
        }
    }

    private fun convergeSources(
        posts : List<PostModel>?,
        comments : List<CommentModel>?,
        tab : UserTab
    ) : List<ListingItem> {
        var listingItems = listOf<ListingItem>()

        if (posts == null && comments != null) {
            listingItems = comments
        }
        else if (comments == null && posts != null) {
            listingItems = posts
        }
        else if (comments != null && posts != null)  {
            listingItems = mutableListOf()
            listingItems.addAll(posts)
            listingItems.addAll(comments)

            listingItems.sortWith(Comparator { o1, o2 ->
                // TODO switch to use a single field for position and return the appropriate position based on the dao query
                val firstP = extractTabPosition(o1, tab)
                val secondP = extractTabPosition(o2, tab)

                firstP - secondP
            })
        }

        Log.d(TAG, "saved positions " + listingItems.map {
            var postTitle = ""
            if (it is PostModel) {
                postTitle = it.title
            } else if (it is CommentModel) {
                postTitle = it.body.substring(0, 10)
            }
            "${it.userSavedPosition} $postTitle"}
        )

        return listingItems
    }

    private fun extractTabPosition(listingItem: ListingItem, tab: UserTab) : Int {
        return when (tab) {
            UserTab.Submitted -> listingItem.userSubmittedPosition
            UserTab.Comments -> listingItem.userCommentsPosition
            UserTab.Saved -> listingItem.userSavedPosition
            UserTab.Upvoted -> listingItem.userUpvotedPosition
            UserTab.Downvoted -> listingItem.userDownvotedPosition
            UserTab.Gilded -> listingItem.userGildedPosition
            UserTab.Hidden -> listingItem.userHiddenPosition
        }
    }

    // endregion helper functions

    // region post adapter delegate

    override fun visitListing(listingItem : ListingItem) {
        launch(Dispatchers.Main) { postGateway.visitPost(listingItem.fullName) }

        // retrieval option doesn't matter in this case
        val postSource = PostRepository.PostSource.User(username, PostRepository.RetrievalOption.Submitted)

        val navData = when (listingItem) {
            is PostModel -> SubNavigationData.ToPost(
                    listingItem.fullName,
                    listingItem.subreddit!!,
                    postSource
            )
            is CommentModel -> SubNavigationData.ToPost(
                    listingItem.parentPost!!,
                    listingItem.subreddit!!,
                    postSource,
                    listingItem.fullName
            )
            else -> null
        }

        navData?.let {
            _navigationLiveData.postValue(RelicEvent(it))
        }
    }

    override fun voteOnListing(listingItem : ListingItem, newVote : Int) {
        launch(Dispatchers.Main) { postGateway.voteOnPost(listingItem.fullName, newVote) }
    }

    override fun saveListing(listingItem : ListingItem) {
        launch(Dispatchers.Main) { postGateway.savePost(listingItem.fullName, !listingItem.saved) }
    }

    override fun onThumbnailClicked(listingItem : ListingItem) {
        if (listingItem is PostModel){
            val isImage = ImageHelper.isValidImage(listingItem.url!!)

            val subNavigation : SubNavigationData = if (isImage) {
                SubNavigationData.ToImage(listingItem.url!!)
            } else {
                SubNavigationData.ToExternal(listingItem.url!!)
            }

            _navigationLiveData.postValue(RelicEvent(subNavigation))
        }
    }

    override fun onUserClicked(listingItem: ListingItem) {
        // TODO only open another user if it's not the current user being displayed
    }
    // endregion post adapter delegate

    override fun handleException(context: CoroutineContext, e: Throwable) {
        Log.e(TAG, "handling e", e)
    }
}