package com.relic.presentation.displaypost

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.util.Log

import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.models.CommentModel
import com.relic.data.models.PostModel
import com.relic.util.RelicError
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private val _navigationLiveData = SingleLiveData<PostNavigationData> ()
    private val _refreshingLiveData = MutableLiveData<Boolean> ()

    private val commentListingKey = MediatorLiveData<String> ()
    private val error = MutableLiveData<RelicError>()

    val postLiveData : LiveData<PostModel> =_postLiveData
    val commentListLiveData : LiveData<List<CommentModel>> = _commentListLiveData
    val postNavigationLiveData : LiveData<PostNavigationData> = _navigationLiveData
    val refreshingLiveData : LiveData<Boolean> = _refreshingLiveData

    init {
        observeLivedata()
        // check internet connection and retrieve more comments if internet is available
        retrieveMoreComments(true)
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
                _commentListLiveData.postValue(comments)
                if (it.isEmpty()) {
                    // retrieve more comments if we detect that none are stored locally
                    //commentRepo.retrieveComments(subName, postFullname, null)
                } else {
                    // TODO add additional actions to trigger when comments loaded
                    _refreshingLiveData.postValue(false)
                }
            }
        }
    }

    override fun retrieveMoreComments(refresh: Boolean) {
        // TODO check if there is connection
        // retrieves post and comments from network
        if (refresh) {
            _refreshingLiveData.postValue(true)
            commentRepo.clearComments(postFullname)
            postRepo.retrievePost(subName, postFullname)
        }
        commentRepo.retrieveComments(subName, postFullname, refresh)
    }

    private fun insertReplies (position : Int, replies : List<CommentModel>) {
        _commentListLiveData.value?.let { commentList ->
            _commentListLiveData.postValue(commentList.toMutableList().apply {
                addAll(position + 1, replies)
            })
        }
    }

    private fun removeReplies (position : Int) {
        _commentListLiveData.value?.toMutableList()?.let { commentList ->
            val parentDepth = commentList[position].depth
            var itemsToRemove = 0

            // hide anything with depth > item.depth until reaching another item of the same depth
            var comparePosition = position + 1
            while (comparePosition < commentList.size && commentList[comparePosition].depth > parentDepth) {
                // add up number of items to be removed
                itemsToRemove ++
                comparePosition ++
            }

            commentList.subList(position + 1, position + 1 + itemsToRemove).clear()
            _commentListLiveData.postValue(commentList)
        }
    }

    // -- region view action delegate --

    override fun onExpandReplies(position: Int, expanded : Boolean) {
        val commentModel = _commentListLiveData.value!![position]

        if (expanded) {
            removeReplies(position)
        } else {
            val commentSource = commentRepo.getReplies(commentModel.id)
            _commentListLiveData.addSource(commentSource) { replies ->
                replies?.let {
                    if (it.isNotEmpty()) {
                        insertReplies(position, it)
                        // TODO switch to observer, which can be removed after comment retrieved
                    } else {
                        // TODO retrieve comments from server if replies are not loaded
                    }
                    // remove this as a source since this is a one off to retrieve replies
                    _commentListLiveData.removeSource(commentSource)
                }
            }
        }
    }

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
            postRepo.postGateway.voteOnPost(commentModel.fullName, voteValue)
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
