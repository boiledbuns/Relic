package com.relic.presentation.displayuser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.relic.api.response.Listing
import com.relic.data.*
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.helper.ImageHelper
import com.relic.presentation.util.RelicEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/// the user is guaranteed to be non-null for this viewmodel
class DisplayUserVM(
    private val postRepo: PostRepository,
    private val userRepo: UserRepository,
    private val postGateway: PostGateway,
    private val username: String?
) : RelicViewModel(), DisplayUserContract.ListingItemAdapterDelegate {

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val userRepo: UserRepository,
        private val postGateway: PostGateway
    ) {
        /*
           specify null username to get the current user
         */
        fun create(username: String?): DisplayUserVM {
            return DisplayUserVM(postRepo, userRepo, postGateway, username)
        }
    }

    private var _userLiveData = MutableLiveData<UserModel>()
    var userLiveData: LiveData<UserModel> = _userLiveData

    private var _navigationLiveData = MutableLiveData<RelicEvent<NavigationData>>()
    var navigationLiveData: LiveData<RelicEvent<NavigationData>> = _navigationLiveData

    private var _errorLiveData = MutableLiveData<ErrorData>()
    val errorLiveData: LiveData<ErrorData> = _errorLiveData

    // dictionary mapping a tab to its associated livedata
    // items should only be added if the tab is being accessed for the first time
    private var postsLiveData = mutableMapOf<UserTab, MediatorLiveData<List<ListingItem>>>()

    private var currentSortingType = mutableMapOf<UserTab, SortType>()
    private var currentSortingScope = mutableMapOf<UserTab, SortScope>()
    private var currentAfterValues = mutableMapOf<UserTab, String?>()

    private lateinit var currentTab: UserTab
    private val retrieveUserJob: Job

    init {
        for (tabType in tabTypes) {
            postsLiveData[tabType] = MediatorLiveData()
        }

        retrieveUserJob = launch(Dispatchers.Main) {
            val user = username?.let { userRepo.retrieveUser(username) }
                ?: userRepo.retrieveCurrentUser()
            // use set value here to dispatch the results immediately
            _userLiveData.value = user
        }
    }

    fun getTabPostsLiveData(tab: UserTab): LiveData<List<ListingItem>> {
        return postsLiveData[tab]!!.apply {
            if (value == null) {
                retrieveUserJob.invokeOnCompletion { requestPosts(tab = tab, refresh = true) }
            }
        }
    }

    fun requestPosts(tab: UserTab, refresh: Boolean) {
        // subscribe to the appropriate livedata based on tab selected
        val userRetrievalOption = toRetrievalOption(tab)
        val postSource = PostSource.User(_userLiveData.value!!.fullName, userRetrievalOption)

        launch(Dispatchers.Main) {
            val listing = if (refresh) {
                val type = currentSortingType[tab] ?: SortType.DEFAULT
                val scope = currentSortingScope[tab] ?: SortScope.NONE

                postRepo.retrieveUserListing(postSource, type, scope)
            } else {
                val listingAfter = currentAfterValues[tab]
                // only retrieve more posts if after is not null
                if (listingAfter != null) {
                    postRepo.retrieveNextListing(source = postSource, after = listingAfter)
                } else null
            }

            if (listing != null) {
                handleListingRetrieval(tab, listing)
            }

            // TODO based on user preferences -> save data offline
        }
    }

    private fun handleListingRetrieval(tab: UserTab, listing: Listing<out ListingItem>) {
        listing.data.children?.let { items ->
//            Log.d(TAG, "listing after ${listing.data.after}")
//            Log.d(TAG, "listing after tab ${currentAfterValues[tab]}")
            val currentList = postsLiveData[tab]!!.value ?: emptyList()

            if (items.isEmpty()) {
                _errorLiveData.postValue(ErrorData.NoMorePosts(tab))
            } else {
                postsLiveData[tab]!!.postValue(currentList.plus(items))
            }
            // only update the "after" val for this tab if successful
            currentAfterValues[tab] = listing.data.after
        }
    }

    fun setCurrentTab(tab: UserTab) {
        currentTab = tab
    }

    fun changeSortingMethod(sortType: SortType? = null, sortScope: SortScope? = null) {
        sortType?.let { currentSortingType[currentTab] = it }
        sortScope?.let { currentSortingScope[currentTab] = it }

        when (currentSortingType[currentTab]) {
            // these sorting types don't have a scope, so retrieve sorted scopes asap
            SortType.NEW, SortType.BEST, SortType.CONTROVERSIAL -> {
                requestPosts(currentTab, refresh = true)
            }
        }

        when (currentSortingScope[currentTab]) {
            // when the sorting scope is changed
            SortScope.HOUR, SortScope.DAY, SortScope.WEEK,
            SortScope.MONTH, SortScope.YEAR, SortScope.ALL -> {
                requestPosts(currentTab, refresh = true)
            }
        }
    }

    // region helper functions

    private fun toRetrievalOption(tab: UserTab): RetrievalOption {
        return when (tab) {
            is UserTab.Submitted -> RetrievalOption.Submitted
            is UserTab.Comments -> RetrievalOption.Comments
            is UserTab.Saved -> RetrievalOption.Saved
            is UserTab.Upvoted -> RetrievalOption.Upvoted
            is UserTab.Downvoted -> RetrievalOption.Downvoted
            is UserTab.Gilded -> RetrievalOption.Gilded
            is UserTab.Hidden -> RetrievalOption.Hidden
        }
    }

    private fun extractTabPosition(listingItem: ListingItem, tab: UserTab): Int {
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

    override fun visitListing(listingItem: ListingItem) {
        launch(Dispatchers.Main) { postGateway.visitPost(listingItem.fullName) }

        // retrieval option doesn't matter in this case
//        val postSource = PostSource.User(username, RetrievalOption.Submitted)

        val navData = when (listingItem) {
            is PostModel -> NavigationData.ToPost(
                listingItem.fullName,
                listingItem.subreddit!!
            )
            is CommentModel -> NavigationData.ToPost(
                listingItem.linkFullname!!,
                listingItem.subreddit!!,
                listingItem.fullName
            )
            else -> null
        }

        navData?.let {
            _navigationLiveData.postValue(RelicEvent(it))
        }
    }

    override fun voteOnListing(listingItem: ListingItem, newVote: Int) {
        launch(Dispatchers.Main) { postGateway.voteOnPost(listingItem.fullName, newVote) }
    }

    override fun saveListing(listingItem: ListingItem) {
        launch(Dispatchers.Main) { postGateway.savePost(listingItem.fullName, !listingItem.saved) }
    }

    override fun onThumbnailClicked(listingItem: ListingItem) {
        if (listingItem is PostModel) {
            val isImage = ImageHelper.isValidImage(listingItem.url!!)

            val subNavigation: NavigationData = if (isImage) {
                NavigationData.ToImage(listingItem.url!!)
            } else {
                NavigationData.ToExternal(listingItem.url!!)
            }

            _navigationLiveData.postValue(RelicEvent(subNavigation))
        }
    }

    override fun onUserClicked(listingItem: ListingItem) {
        // TODO only open another user if it's not the current user being displayed
    }
    // endregion post adapter delegate

    override fun handleException(context: CoroutineContext, e: Throwable) {
//        Log.e(TAG, "caught exception", e)
    }
}