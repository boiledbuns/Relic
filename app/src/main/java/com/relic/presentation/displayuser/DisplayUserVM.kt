package com.relic.presentation.displayuser

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.models.ListingItem
import com.relic.data.models.PostModel
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class DisplayUserVM(
    private val postRepo: PostRepository,
    private val commentRepo: CommentRepository,
    private val listingRepo: ListingRepository,
    private val username : String
) : ViewModel(), DisplaySubContract.PostAdapterDelegate {

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val commentRepo: CommentRepository,
        private val listingRepo: ListingRepository
    ) {
        fun create(username : String) : DisplayUserVM {
            return DisplayUserVM(postRepo, commentRepo, listingRepo, username)
        }
    }

    private var currentSortingType = emptyMap<UserTab, PostRepository.SortType>()
    private var currentSortingScope = emptyMap<UserTab, PostRepository.SortScope>()

    private var postsLiveData = mutableMapOf<UserTab, MediatorLiveData<List<ListingItem>>>()

    fun getTabPostsLiveData(tab : UserTab) : LiveData<List<ListingItem>> {
        var tabLiveData = postsLiveData[tab]
        val userRetrievalOption = toRetrievalOption(tab)

        if (tabLiveData == null) {
            val postSource = postRepo.getPosts(PostRepository.PostSource.User(username, userRetrievalOption))

            // create a new livedata if it doesn't already exist
            tabLiveData = MediatorLiveData<List<ListingItem>>().apply {
                addSource(postSource) {

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

    private fun convergeSources() {

    }

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