package com.relic.presentation.displaysub

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log

import com.relic.data.PostRepository
import com.relic.data.SubRepository
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.data.models.PostModel
import com.relic.data.models.SubredditModel
import com.shopify.livedataktx.SingleLiveData
import javax.inject.Inject

open class DisplaySubVM (
        private val postSource: PostRepository.PostSource,
        private val subRepo: SubRepository,
        private var postRepo: PostRepository
) : ViewModel(), DisplaySubContract.ViewModel, DisplaySubContract.PostAdapterDelegate, RetrieveNextListingCallback {

    class Factory @Inject constructor(
            private val subRepo: SubRepository,
            private val postRepo : PostRepository
    ) {
        fun create (postSource : PostRepository.PostSource) : DisplaySubVM {
            return DisplaySubVM(postSource, subRepo, postRepo)
        }
    }

    private val TAG = "DISPLAY_SUB_VM"
    private var currentSortingType = PostRepository.SORT_DEFAULT
    private var currentSortingScope = PostRepository.SCOPE_NONE

    private val _subredditMediator = MediatorLiveData<SubredditModel>()
    val subredditLiveData : LiveData<SubredditModel> = _subredditMediator

    private val _postListMediator= MediatorLiveData<List<PostModel>> ()
    val postListLiveData : LiveData<List<PostModel>> = _postListMediator

    private val _navigationLiveData = SingleLiveData<NavigationData>()
    val navigationLiveData : LiveData<NavigationData> = _navigationLiveData

    private val _subInfoLiveData = MutableLiveData<DisplaySubInfoData>()
    val subInfoLiveData : LiveData<DisplaySubInfoData> = _subInfoLiveData

    init {
        // observe the list of posts stored locally
        _postListMediator.addSource(postRepo.getPosts(postSource)) { postModels ->
            // retrieve posts when the posts stored locally for this sub have been cleared
            if (postModels != null && postModels.isEmpty()) {
                Log.d(TAG, "Local posts have been emptied -> retrieving more posts")
                // clears current posts for this subreddit and retrieves new ones based on current sorting method and scope
                postRepo.retrieveSortedPosts(postSource, currentSortingType, currentSortingScope)
                // TODO add a liveData boolean success listener
                // TODO add a flag for the to check if retrieval occured
            } else {
                Log.d(TAG, postModels!!.size.toString() + " posts retrieved were from the network")
                _postListMediator.setValue(postModels)
            }
        }

        when (postSource) {
            is PostRepository.PostSource.Subreddit -> initializeSubredditInformation(postSource.subredditName)
            is PostRepository.PostSource.Frontpage -> {}
            is PostRepository.PostSource.All -> {}
        }

        _subInfoLiveData.postValue(DisplaySubInfoData(
            sortingMethod = currentSortingType,
            sortingScope = currentSortingScope
        ))
    }

    private fun initializeSubredditInformation(subName : String ) {
        // TODO: STILL TESTING retrieve the banner image from the subredddit css
        subRepo.subGateway.apply {
            getAdditionalSubInfo(subName)
            getSidebar(subName)
        }

        //subRepo.getSubGateway().retrieveSubBanner(subName);
        _subredditMediator.addSource(subRepo.getSingleSub(subName)) { newModel ->
            if (newModel == null) {
                Log.d(TAG, "No subreddit saved locally, retrieving from network")
                subRepo.retrieveSingleSub(subName)
            } else {
                Log.d(TAG, "Subreddit loaded " + newModel.getBannerUrl())
                _subredditMediator.setValue(newModel)
            }
        }
    }

    /**
     * Method to retrieve more posts
     * @param resetPosts : indicates whether the old posts should be cleared
     */
    override fun retrieveMorePosts(resetPosts: Boolean) {
        if (resetPosts) {
            // all we have to do is clear entries in room -> our observer for the posts will auto download new posts when it's empty
            postRepo.clearAllPostsFromSource(postSource)
        } else {
            // retrieve the "after" value for the next posting
            postRepo.getNextPostingVal(this, postSource)
        }
    }

    /**
     * Called when the user has changed an aspect of the sorting method for this sub. Null values
     * for either the sortType or sortScope indicate no change
     * @param sortType : code corresponding to sort type
     * @param sortScope : code corresponding to sort scope
     */
    override fun changeSortingMethod(sortType: Int?, sortScope: Int?) {
        // update the current sorting method and scope if it has changed
        sortType?.let { currentSortingType = it }
        sortScope?.let { currentSortingScope = it }

        // remove all posts from current db for this subreddit (triggers retrieval)
        postRepo.clearAllPostsFromSource(postSource)
        postRepo.retrieveSortedPosts(postSource, currentSortingType, currentSortingScope)
        _subInfoLiveData.postValue(
            DisplaySubInfoData(sortingMethod = currentSortingType, sortingScope = currentSortingScope)
        )
    }

    override fun onNextListing(nextVal: String?) {
        Log.d(TAG, "Retrieving next posts with $nextVal")
        // retrieve the "after" value for the next posting
        nextVal?.let {
            postRepo.retrieveMorePosts(postSource, it)
        }
    }

    override fun updateSubStatus(toSubscribe: Boolean) {
        if (postSource is PostRepository.PostSource.Subreddit) {
            val subName = postSource.subredditName
            Log.d(TAG, "Changing to subscribed $toSubscribe")
            if (toSubscribe) {
                // subscribe if not currently subscribed
                val successObservable = subRepo.subGateway.subscribe(subName)
                _subredditMediator.addSource(successObservable) { success: Boolean? ->

                    if (success != null && success) {
                        Log.d(TAG, "subscribing")
                    }
                    // unsubscribe after consuming event
                    _subredditMediator.removeSource(successObservable)
                }
            } else {
                // unsubscribe if already subscribed
                val successObservable = subRepo.subGateway.unsubscribe(subName)
                _subredditMediator.addSource(successObservable) { success: Boolean? ->

                    if (success != null && success) {
                        Log.d(TAG, "unsubscribing")
                        //subMediator.setValue(false);
                    }

                    //subscribed.setValue(success);
                    _subredditMediator.removeSource(successObservable)
                }
            }
        }
    }

    // region view action delegate

    override fun visitPost(postfullName : String, postSubreddit : String) {
        postRepo.postGateway.visitPost(postfullName)

//        _navigationLiveData.apply {
//            value = NavigationData.ToPost(postfullName, postSubreddit)
//            value = null
//        }
        _navigationLiveData.value = NavigationData.ToPost(postfullName, postSubreddit)
    }

    override fun voteOnPost(postFullname: String, voteValue: Int) {
        Log.d(TAG, "Voting on post " + postFullname + "value = " + voteValue)
        postRepo.postGateway.voteOnPost(postFullname, voteValue)
    }

    override fun savePost(postFullname: String, save: Boolean) {
        Log.d(TAG, "Saving on post " + postFullname + "save = " + save)
        postRepo.postGateway.savePost(postFullname, save)
    }

    override fun onThumbnailClicked(postThumbnailUrl: String) {
        val validImageEndings = listOf("jpg", "png", "gif")

        val lastThree = postThumbnailUrl.substring(postThumbnailUrl.length - 3)
        var isImage = (validImageEndings.contains(lastThree))

        val navigation : NavigationData = if (isImage) {
            NavigationData.ToImage(postThumbnailUrl)
        } else {
            NavigationData.ToExternal(postThumbnailUrl)
        }

        _navigationLiveData.apply {
            value = navigation
            value = null
        }
    }
    // endregion view action delegate

    // region sorting type helper functions

    companion object {
        fun convertSortingTypeToText(sortByCode : Int) : String  {
            return when(sortByCode) {
                PostRepository.SORT_DEFAULT-> "best"
                PostRepository.SORT_HOT -> "hot"
                PostRepository.SORT_NEW -> "new"
                PostRepository.SORT_RISING-> "rising"
                PostRepository.SORT_TOP -> "top"
                PostRepository.SORT_CONTROVERSIAL -> "controversial"
                else -> "default"
            }
        }

        fun convertSortingScopeToText(sortByScope : Int) : String? {
            return when(sortByScope) {
                PostRepository.SCOPE_HOUR-> "hour"
                PostRepository.SCOPE_DAY -> "day"
                PostRepository.SCOPE_WEEK -> "week"
                PostRepository.SCOPE_MONTH-> "month"
                PostRepository.SCOPE_YEAR -> "year"
                PostRepository.SCOPE_ALL -> "all"
                else -> null
            }
        }
    }
    // endregion sorting type helper functions
}
