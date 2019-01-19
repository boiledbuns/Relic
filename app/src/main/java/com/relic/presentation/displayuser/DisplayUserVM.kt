package com.relic.presentation.displayuser

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
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

    private var postsLiveData = mutableMapOf<UserTab, LiveData<List<PostModel>>>()

    init {
//        GlobalScope.launch {
//            postRepo.retrieveUserPosts(username)
//        }
    }

    fun getTabPostsLiveData(tab : UserTab) : LiveData<List<PostModel>> {
        var tabLiveData = postsLiveData[tab]
        val userRetrievalOption = convertTabToRetrievalOption(tab)

        if (tabLiveData == null) {
            // create a new livedata if it doesn't already exist
            tabLiveData = postRepo.getUserPosts(username, userRetrievalOption)
            postsLiveData[tab] = tabLiveData
        }

        return tabLiveData
    }

    /**
     *
     */
    fun requestPosts(tab : UserTab, refresh : Boolean) {
        // subscribe to the appropriate livedata based on tab selected
        val postSource = PostRepository.PostSource.User(username)
        val userRetrievalOption = convertTabToRetrievalOption(tab)

        GlobalScope.launch {
            if (refresh) {
                runBlocking { postRepo.clearAllPostsFromSource(postSource) }
                postRepo.retrieveUserPosts(username, userRetrievalOption)
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

    private fun convertTabToRetrievalOption(tab : UserTab): PostRepository.RetrievalOption {
        return when (tab) {
            is UserTab.Submissions -> PostRepository.RetrievalOption.Submissions
            is UserTab.Comments -> PostRepository.RetrievalOption.Comments
            is UserTab.Saved -> PostRepository.RetrievalOption.Saved
            is UserTab.Upvoted -> PostRepository.RetrievalOption.Upvoted
            is UserTab.Downvoted -> PostRepository.RetrievalOption.Downvoted
            is UserTab.Gilded -> PostRepository.RetrievalOption.Gilded
            is UserTab.Hidden -> PostRepository.RetrievalOption.Hidden
        }
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