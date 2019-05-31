package com.relic.presentation.displayuser

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.UserRepository
import com.relic.data.models.CommentModel
import com.relic.data.models.ListingItem
import com.relic.data.models.PostModel
import com.relic.data.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.presentation.displaysub.SubNavigationData
import com.relic.presentation.helper.ImageHelper
import com.relic.util.RelicEvent
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

class DisplayUserVM(
    private val postRepo: PostRepository,
    private val commentRepo: CommentRepository,
    private val listingRepo: ListingRepository,
    private val userRepo: UserRepository,
    private val username : String
) : RelicViewModel(), DisplayUserContract.ListingItemAdapterDelegate {

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val commentRepo: CommentRepository,
        private val listingRepo: ListingRepository,
        private val userRepo: UserRepository
    ) {
        fun create(username : String) : DisplayUserVM {
            return DisplayUserVM(postRepo, commentRepo, listingRepo, userRepo, username)
        }
    }

    private var _userLiveData = MutableLiveData<UserModel>()
    var userLiveData : LiveData<UserModel> = _userLiveData

    private var _navigationLiveData = MutableLiveData<RelicEvent<SubNavigationData>>()
    var navigationLiveData : LiveData<RelicEvent<SubNavigationData>> = _navigationLiveData

    private var postsLiveData = mutableMapOf<UserTab, MediatorLiveData<List<ListingItem>>>()

    private var currentSortingType = mutableMapOf<UserTab, PostRepository.SortType>()
    private var currentSortingScope = mutableMapOf<UserTab, PostRepository.SortScope>()
    private lateinit var currentTab : UserTab

    // used to store the posts and comments since they are retrieved separately before converging
    private var postLists = mutableMapOf<UserTab, List<PostModel>>()
    private var commentLists = mutableMapOf<UserTab, List<CommentModel>>()

    init {
        launch(Dispatchers.Main) {
            _userLiveData.postValue(userRepo.retrieveUser(username))
        }
    }

    fun getTabPostsLiveData(tab : UserTab) : LiveData<List<ListingItem>> {
        launch(Dispatchers.Main) {
            _userLiveData.postValue(userRepo.retrieveUser(username))
        }
        var tabLiveData = postsLiveData[tab]
        val userRetrievalOption = toRetrievalOption(tab)

        if (tabLiveData == null) {
            // request new posts if this is the first time the tab is being created
            // TODO need to separate the process of deleting old posts from retrieving new posts
            // TODO to prevent the old posts being deleted --> internet connection unavailable --> so new posts
            requestPosts(tab = tab, refresh = true)

            val postSource = postRepo.getPosts(PostRepository.PostSource.User(username, userRetrievalOption))
            val commentSource = commentRepo.getComments(userRetrievalOption)

            // create a new livedata if it doesn't already exist
            tabLiveData = MediatorLiveData<List<ListingItem>>().apply {
                // TODO add a diff util
                addSource(postSource) { newList ->
                    if (newList!= null && newList.isNotEmpty()) {
                        postLists[tab] = newList
                        // TODO move this logic to repository. We shouldn't care about this
                        // TODO tbh consider moving the entire convergence method outside of vm
                        // only post source if all posts and comments are in loaded in order
                        convergeSources(postLists[tab], commentLists[tab], tab).let { converged ->
                            if (extractTabPosition(converged.last(), tab) == converged.size - 1) {
                                this.postValue(converged)
                            }
                        }
                    }
                }
                addSource(commentSource) { newList ->
                    if (newList!= null && newList.isNotEmpty()) {
                        commentLists[tab] = newList
                        // only post source if all posts and comments are in loaded in order
                        convergeSources(postLists[tab], commentLists[tab], tab).let { converged ->
                            if (extractTabPosition(converged.last(), tab) == converged.size - 1) {
                                this.postValue(converged)
                            }
                        }
                    }
                }
            }
            postsLiveData[tab] = tabLiveData
        }

        return tabLiveData
    }

    /**
     *
     */
    fun requestPosts(tab : UserTab, refresh : Boolean) {
        // subscribe to the appropriate livedata based on tab selected
        val userRetrievalOption = toRetrievalOption(tab)
        val postSource = PostRepository.PostSource.User(username, userRetrievalOption)

        val type = currentSortingType[tab] ?: PostRepository.SortType.DEFAULT
        val scope = currentSortingScope[tab] ?: PostRepository.SortScope.NONE

        launch(Dispatchers.Main) {
            if (refresh) {
                runBlocking { postRepo.clearAllPostsFromSource(postSource) }
                postRepo.retrieveSortedPosts(postSource, type, scope)
            } else {
                // not a fan of this design, because it requires the viewmodel to be aware of the
                // "key" being used to store the "after" value which is an implementation detail.
                // TODO consider refactoring later, for now be consistent
                postRepo.getNextPostingVal(
                    postSource = postSource,
                    callback = RetrieveNextListingCallback { key ->
                        launch { postRepo.retrieveMorePosts(postSource, key) }
                    }
                )
            }
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
        launch(Dispatchers.Main) { postRepo.postGateway.visitPost(listingItem.fullName) }

        // retrieval option doesn't matter in this case
        val postSource = PostRepository.PostSource.User(username, PostRepository.RetrievalOption.Submitted)

        val navData = when (listingItem) {
            is PostModel -> SubNavigationData.ToPost(
                    listingItem.fullName,
                    listingItem.subreddit!!,
                    postSource
            )
            is CommentModel -> SubNavigationData.ToPost(
                    PostModel.TYPE + "_" + listingItem.parentPostId,
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
        launch(Dispatchers.Main) { postRepo.postGateway.voteOnPost(listingItem.fullName, newVote) }
    }

    override fun saveListing(listingItem : ListingItem) {
        launch(Dispatchers.Main) { postRepo.postGateway.savePost(listingItem.fullName, !listingItem.saved) }
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