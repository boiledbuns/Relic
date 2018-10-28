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
import javax.inject.Inject

class DisplaySubVM (
        private val subName: String,
        private val subRepo: SubRepository,
        private var postRepo: PostRepository
) : ViewModel(), DisplaySubContract.ViewModel,
        DisplaySubContract.PostAdapterDelegate, RetrieveNextListingCallback {

    class Factory @Inject constructor(
            private val subRepo: SubRepository,
            private val postRepo : PostRepository
    ) {
        fun create (subName : String) : DisplaySubVM {
            return DisplaySubVM(subName, subRepo, postRepo)
        }
    }

    private val TAG = "DISPLAY_SUB_VM"
    private var currentSortingType = PostRepository.SORT_DEFAULT
    private var currentSortingScope = PostRepository.SCOPE_NONE

    private val _subredditMediator = MediatorLiveData<SubredditModel>()
    val subredditLivedata : LiveData<SubredditModel> = _subredditMediator

    private val _postListMediator= MediatorLiveData<List<PostModel>> ()
    val postListLiveData : LiveData<List<PostModel>> = _postListMediator

    private val _navigationLivedata = MutableLiveData<NavigationData>()
    val navigationLivedata : LiveData<NavigationData> = _navigationLivedata

    init {
        // observe the list of posts stored locally
        _postListMediator.addSource(postRepo.getPosts(subName)) { postModels ->
            // retrieve posts when the posts stored locally for this sub have been cleared
            if (postModels != null && postModels.isEmpty()) {
                Log.d(TAG, "Local posts have been emptied -> retrieving more posts")
                // clears current posts for this subreddit and retrieves new ones based on current sorting method and scope
                postRepo.retrieveSortedPosts(subName, currentSortingType, currentSortingScope)
                // TODO add a livedata boolean success listener
                // TODO add a flag for the to check if retrieval occured
            } else {
                Log.d(TAG, postModels!!.size.toString() + " posts retrieved were from the network")
                _postListMediator.setValue(postModels)
            }
        }

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
     * Method that allows views aware of the VM to request the VM retrieve more posts
     */
    override fun retrieveMorePosts(resetPosts: Boolean) {
        if (resetPosts) {
            // all we have to do is clear entries in room -> our observer for the posts will auto download new posts when it's empty
            postRepo.clearAllSubPosts(subName)
        } else {
            // retrieve the "after" value for the next posting
            postRepo.getNextPostingVal(this, subName)
        }
    }

    override fun changeSortingMethod(sortingCode: Int, sortScope: Int) {
        // update the current sorting method and scope
        currentSortingType = sortingCode
        currentSortingScope = sortScope

        // remove all posts from current db for this subreddit (triggers retrieval)
        postRepo.clearAllSubPosts(subName)
    }

    override fun onNextListing(nextVal: String?) {
        Log.d(TAG, "Retrieving next posts with $nextVal")
        // retrieve the "after" value for the next posting
        nextVal?.let {
            postRepo.retrieveMorePosts(subName, it)
        }
    }

    override fun updateSubStatus(toSubscribe: Boolean) {
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


    // region view action delegate

    override fun visitPost(postFullname: String) {
        postRepo.postGateway.visitPost(postFullname)

        _navigationLivedata.apply {
            value = NavigationData.ToPost(postFullname, subName)
            value = null
        }
    }

    override fun voteOnPost(postFullname: String, voteValue: Int) {
        Log.d(TAG, "Voting on post " + postFullname + "value = " + voteValue)
        postRepo.postGateway.voteOnPost(postFullname, voteValue)
    }

    override fun savePost(postFullname: String, save: Boolean) {
        Log.d(TAG, "Saving on post " + postFullname + "save = " + save)
        postRepo.postGateway.savePost(postFullname, save)
    }

    override fun showImage(postThumbnailUrl: String) {
        _navigationLivedata.apply {
            value = NavigationData.ToImage(postThumbnailUrl)
            value = null
        }
    }

    // end region view action delegate
}
