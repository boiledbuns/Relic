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

    private val postLiveData = MediatorLiveData<PostModel> ()
    private val commentListLiveData = MediatorLiveData<List<CommentModel>> ()
    private val _imageUrl = MutableLiveData<String> ()

    private val commentListingKey = MediatorLiveData<String> ()
    private val error = MutableLiveData<RelicError>()

    init {
        refresh()
        observeLivedata()
        //retrieveMoreComments(true)
    }

    /**
     * Exposes the post to the view
     * @return post as livedata
     */
    override fun getPost(): LiveData<PostModel> {
        return postLiveData
    }

    /**
     * Exposes the list of comments to the view
     * @return comment list as livedata
     */
    override fun getCommentList(): LiveData<List<CommentModel>> {
        return commentListLiveData
    }

    /**
     * Add sources and listeners to all local livedata
     */
    private fun observeLivedata() {
        // retrieves the livedata post to be exposed to the view
        postLiveData.addSource <PostModel>(postRepo.getPost(postFullname)) {
            postLiveData.postValue(it)
        }

        // retrieve the comment list as livedata so that we can expose it to the view first
        commentListLiveData.addSource(commentRepo.getComments(postFullname)) { comments ->
            comments?.let {
                if (it.isEmpty()) {
                    // retrieve more comments if we detect that none are stored locally
                    commentRepo.retrieveComments(subName, postFullname, null)
                } else {
                    // TODO add additional actions to trigger when comments loaded
                    commentListLiveData.postValue(comments)
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

    override fun voteOnPost(postFullname: String, voteValue: Int) {
        Log.d(TAG, "Voted on post " + postFullname + "value = " + voteValue)
        postRepo.postGateway.voteOnPost(postFullname, voteValue)
    }

    fun isImage(): Boolean {
        var isImage = false

        postLiveData.value?.url?.let {
            val lastThree = it.substring(it.length - 3)
            if (validImageEndings.contains(lastThree)) isImage = true
        }

        return isImage
    }
}
