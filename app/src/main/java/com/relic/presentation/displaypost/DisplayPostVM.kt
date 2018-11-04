package com.relic.presentation.displaypost

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log

import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.models.CommentModel
import com.relic.data.models.PostModel
import com.relic.util.RelicError
import com.shopify.livedataktx.SingleLiveData
import javax.inject.Inject

class DisplayPostVM (
        private val postRepo : PostRepository,
        private val commentRepo: CommentRepository,
        private val listingRepo: ListingRepository,
        private val subName: String,
        private val postFullname: String
): ViewModel(), DisplayPostContract.ViewModel, DisplayPostContract.PostViewDelegate {

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val commentRepo: CommentRepository,
        private val listingRepo: ListingRepository
    ) {
        fun create(subredditName : String, postFullname : String) : DisplayPostVM {
            return DisplayPostVM(postRepo, commentRepo, listingRepo, subredditName, postFullname)
        }
    }

    private val TAG = "DISPLAYPOST_VM"
    private val validImageEndings = listOf("jpg", "png")

    private val _postLiveData = MediatorLiveData<PostModel> ()
    private val _commentListLiveData = MediatorLiveData<List<CommentModel>> ()
    private val _navigationLiveData = SingleLiveData<PostNavigationData>()

    private val commentListingKey = MediatorLiveData<String> ()
    private val error = MutableLiveData<RelicError>()

    val postLiveData : LiveData<PostModel> =_postLiveData
    val commentListLiveData : LiveData<List<CommentModel>> = _commentListLiveData
    val postNavigationLiveData : LiveData<PostNavigationData> = _navigationLiveData

    init {
        refresh()
        observeLivedata()
        //retrieveMoreComments(true)
    }

    /**
     * Add sources and listeners to all local livedata
     */
    private fun observeLivedata() {
        // retrieves the livedata post to be exposed to the view
        _postLiveData.addSource <PostModel>(postRepo.getPost(postFullname)) {
            _postLiveData.postValue(it)
        }

        // retrieve the comment list as livedata so that we can expose it to the view first
        _commentListLiveData.addSource(commentRepo.getComments(postFullname)) { comments ->
            comments?.let {
                if (it.isEmpty()) {
                    // retrieve more comments if we detect that none are stored locally
                    commentRepo.retrieveComments(subName, postFullname, null)
                } else {
                    // TODO add additional actions to trigger when comments loaded
                    _commentListLiveData.postValue(comments)
                }
            }
        }

        commentListingKey.addSource(listingRepo.key) { nextListingKey ->
            nextListingKey?.let {
                // retrieve the next listing using its key
                commentRepo.retrieveComments(subName, postFullname, it)
            }
        }
    }

    /**
     * Refreshes the post and comment data from network
     */
    override fun refresh() {
        // retrieves post from network
        postRepo.retrievePost(subName, postFullname)
        // retrieves comments form network
        retrieveMoreComments(true)
    }

    override fun retrieveMoreComments(refresh: Boolean) {
        if (refresh) {
            // delete comments stored locally, let observer retrieve more once it registers the change
            commentRepo.clearComments(postFullname)
        } else {
            // retrieve the next listing for the comments on this post
            listingRepo.retrieveKey(postFullname)
        }
    }

    // -- region view action delegate --

    override fun onPostVoted(voteValue: Int) {
        Log.d(TAG, "Voted on post " + postFullname + "value = " + voteValue)
        postRepo.postGateway.voteOnPost(postFullname, voteValue)
    }

    override fun onCommentVoted(commentModel: CommentModel, voteValue: Int) : Int{
        var newUserUpvoteValue = 0
        when (voteValue) {
            UPVOTE_PRESSED -> {
                if (commentModel.userUpvoted != CommentModel.UPVOTE) newUserUpvoteValue = CommentModel.UPVOTE
            }
            DOWNVOTE_PRESSED -> {
                if (commentModel.userUpvoted != CommentModel.DOWNVOTE) newUserUpvoteValue = CommentModel.DOWNVOTE
            }
        }

        // send request only if value changed
        if (newUserUpvoteValue != commentModel.userUpvoted) {
            postRepo.postGateway.voteOnPost(commentModel.id, voteValue)
        }
        return newUserUpvoteValue
    }

    override fun onImagePressed() {
        _navigationLiveData.value = PostNavigationData.ToImage(_postLiveData.value!!.url)
    }

    // -- end region view action delegate --

    fun isImage(): Boolean {
        var isImage = false

        _postLiveData.value?.url?.let {
            val lastThree = it.substring(it.length - 3)
            if (validImageEndings.contains(lastThree)) isImage = true
        }

        return isImage
    }
}
