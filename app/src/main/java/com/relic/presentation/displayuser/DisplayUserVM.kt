package com.relic.presentation.displayuser

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.UserRepository
import com.relic.data.models.CommentModel
import com.relic.data.models.ListingItem
import com.relic.data.models.PostModel
import com.relic.data.models.UserModel
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class DisplayUserVM(
    private val postRepo: PostRepository,
    private val commentRepo: CommentRepository,
    private val listingRepo: ListingRepository,
    private val userRepo: UserRepository,
    private val username : String
) : ViewModel(), DisplaySubContract.PostAdapterDelegate {

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

    private var postsLiveData = mutableMapOf<UserTab, MediatorLiveData<List<ListingItem>>>()

    private var currentSortingType = mutableMapOf<UserTab, PostRepository.SortType>()
    private var currentSortingScope = mutableMapOf<UserTab, PostRepository.SortScope>()
    private lateinit var currentTab : UserTab

    // used to store the posts and comments since they are retrieved separately before converging
    private var postLists = mutableMapOf<UserTab, List<PostModel>>()
    private var commentLists = mutableMapOf<UserTab, List<CommentModel>>()

    init {
        GlobalScope.launch  {
            _userLiveData.postValue(userRepo.retrieveUser(username))
        }
    }

    fun getTabPostsLiveData(tab : UserTab) : LiveData<List<ListingItem>> {
        GlobalScope.launch  {
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
                    newList?.let {
                        postLists[tab] = newList
                        this.postValue(convergeSources(postLists[tab], commentLists[tab], tab))
                    }
                }
                addSource(commentSource) { newList ->
                    newList?.let {
                        commentLists[tab] = newList
                        this.postValue(convergeSources(postLists[tab], commentLists[tab], tab))
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

        GlobalScope.launch {
            if (refresh) {
                runBlocking { postRepo.clearAllPostsFromSource(postSource) }
                postRepo.retrieveSortedPosts(postSource, type, scope)
            } else {
                // not a fan of this design, because it requires the viewmodel to be aware of the
                // "key" being used to store the "after" value which is an implementation detail.
                // TODO consider refactoring later, for now be consistent
                val key = suspendCoroutine<String> { cont ->
                    postRepo.getNextPostingVal(
                        postSource = postSource,
                        callback = RetrieveNextListingCallback { afterVal ->
                            cont.resumeWith(Result.success(afterVal))
                        })
                }
                postRepo.retrieveMorePosts(postSource, key)
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
        val listingItems = mutableListOf<ListingItem>()

        if (posts == null && comments != null) {
            listingItems.addAll(comments)
        }
        else if (comments == null && posts != null) {
            listingItems.addAll(posts)
        }
        else if (comments != null && posts != null)  {
            listingItems.addAll(posts)
            listingItems.addAll(comments)

            listingItems.sortWith(Comparator { o1, o2 ->
                var firstP = 0
                var secondP = 0

                when (tab) {
                    UserTab.Submitted -> {
                        o1.userSubmittedPosition?.let { firstP = it }
                        o2.userSubmittedPosition?.let { secondP = it }
                    }
                    UserTab.Comments -> {
                        o1.userCommentsPosition?.let { firstP = it }
                        o2.userCommentsPosition?.let { secondP = it }
                    }
                    UserTab.Saved -> {
                        o1.userSavedPosition?.let { firstP = it }
                        o2.userSavedPosition?.let { secondP = it }
                    }
                    UserTab.Upvoted -> {
                        o1.userUpvotedPosition?.let { firstP = it }
                        o2.userUpvotedPosition?.let { secondP = it }
                    }
                    UserTab.Downvoted -> {
                        o1.userDownvotedPosition?.let { firstP = it }
                        o2.userDownvotedPosition?.let { secondP = it }
                    }
                    UserTab.Gilded -> {
                        o1.userGildedPosition?.let { firstP = it }
                        o2.userGildedPosition?.let { secondP = it }
                    }
                    UserTab.Hidden -> {
                        o1.userHiddenPosition?.let { firstP = it }
                        o2.userHiddenPosition?.let { secondP = it }
                    }
                }
                if (firstP > secondP) 1 else 0
            })
        }


        return listingItems
    }

    // endregion helper functions

    // region post adapter delegate

    override fun visitPost(postFullname: String, subreddit: String) {
    }

    override fun voteOnPost(postFullname: String, voteValue: Int) {
    }

    override fun savePost(postFullname: String, save: Boolean) {
    }

    override fun onThumbnailClicked(postThumbnailUrl: String) {
    }

    // endregion post adapter delegate
}