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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

open class DisplaySubVM (
    private val postSource: PostRepository.PostSource,
    private val subRepo: SubRepository,
    private var postRepo: PostRepository,
    override val coroutineContext: CoroutineContext = Dispatchers.Default
) : ViewModel(), DisplaySubContract.ViewModel, DisplaySubContract.PostAdapterDelegate, RetrieveNextListingCallback, CoroutineScope {

    class Factory @Inject constructor(
            private val subRepo: SubRepository,
            private val postRepo : PostRepository
    ) {
        fun create (postSource : PostRepository.PostSource) : DisplaySubVM {
            return DisplaySubVM(postSource, subRepo, postRepo)
        }
    }

    private val TAG = "DISPLAY_SUB_VM"
    private var currentSortingType = PostRepository.SortType.DEFAULT
    private var currentSortingScope = PostRepository.SortScope.NONE
    private var retrievalInProgress = false

    private val _subredditMediator = MediatorLiveData<SubredditModel>()
    val subredditLiveData : LiveData<SubredditModel> = _subredditMediator

    private val _postListMediator= MediatorLiveData<List<PostModel>> ()
    val postListLiveData : LiveData<List<PostModel>> = _postListMediator

    private val _navigationLiveData = SingleLiveData<SubNavigationData>()
    val subNavigationLiveData : LiveData<SubNavigationData> = _navigationLiveData

    private val _subInfoLiveData = MutableLiveData<DisplaySubInfoData>()
    val subInfoLiveData : LiveData<DisplaySubInfoData> = _subInfoLiveData

    init {
        // observe the list of posts stored locally
        _postListMediator.addSource(postRepo.getPosts(postSource)) { postModels ->
            // retrieve posts when the posts stored locally for this sub have been cleared
            if (!retrievalInProgress && postModels != null && postModels.isEmpty()) {
                Log.d(TAG, "Local posts have been emptied -> retrieving more posts")
                // clears current posts for this subreddit and retrieves new ones based on current sorting method and scope
                GlobalScope.launch {
                    postRepo.retrieveSortedPosts(postSource, currentSortingType, currentSortingScope)
                }
                retrievalInProgress = true
            } else {
                Log.d(TAG, postModels!!.size.toString() + " posts retrieved were from the network")
                _postListMediator.setValue(postModels)
                retrievalInProgress = false
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
        // TODO: STILL TESTING retrieve the banner image from the subreddit css
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
        GlobalScope.launch {
            if (resetPosts) {
                // all we have to do is clear entries in room -> our observer for the posts will auto download new posts when it's empty
                postRepo.clearAllPostsFromSource(postSource)
            } else {
                // retrieve the "after" value for the next posting
                postRepo.getNextPostingVal(this@DisplaySubVM, postSource)
            }
        }
    }

    /**
     * Called when the user has changed an aspect of the sorting method for this sub. Null values
     * for either the sortType or sortScope indicate no change
     * @param sortType : code corresponding to sort type
     * @param sortScope : code corresponding to sort scope
     */
    override fun changeSortingMethod(sortType: PostRepository.SortType?, sortScope: PostRepository.SortScope?) {
        // update the current sorting method and scope if it has changed
        sortType?.let { currentSortingType = it }
        sortScope?.let { currentSortingScope = it }

        GlobalScope.launch {
            // remove all posts from current db for this subreddit (triggers retrieval)
            postRepo.clearAllPostsFromSource(postSource)
        }
        _subInfoLiveData.postValue(
            DisplaySubInfoData(sortingMethod = currentSortingType, sortingScope = currentSortingScope)
        )
    }

    override fun onNextListing(nextVal: String?) {
        Log.d(TAG, "Retrieving next posts with $nextVal")
        // retrieve the "after" value for the next posting
        nextVal?.let {
            GlobalScope.launch {
                postRepo.retrieveMorePosts(postSource, it)
            }
        }
    }

    override fun updateSubStatus(subscribe: Boolean) {
        if (postSource is PostRepository.PostSource.Subreddit) {
            val subName = postSource.subredditName
            Log.d(TAG, "Changing to subscribed $subscribe")
            if (subscribe) {
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
//        postRepo.postGateway.visitPost(postfullName)
        _navigationLiveData.value = SubNavigationData.ToPost(postfullName, postSubreddit, postSource)
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

        val subNavigation : SubNavigationData = if (isImage) {
            SubNavigationData.ToImage(postThumbnailUrl)
        } else {
            SubNavigationData.ToExternal(postThumbnailUrl)
        }

        _navigationLiveData.apply {
            value = subNavigation
            value = null
        }
    }

    // endregion view action delegate
}
