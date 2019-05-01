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
import com.relic.network.NetworkUtil
import com.relic.network.request.RelicRequestError
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class DisplayPostVM (
    private val postRepo : PostRepository,
    private val commentRepo: CommentRepository,
    private val listingRepo: ListingRepository,
    private val networkUtil : NetworkUtil,
    private val subName: String,
    private val postFullname: String,
    private val postSource : PostRepository.PostSource
): ViewModel(), DisplayPostContract.ViewModel, DisplayPostContract.PostViewDelegate {

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val commentRepo: CommentRepository,
        private val listingRepo: ListingRepository,
        private val networkUtil : NetworkUtil
    ) {
        fun create(subredditName : String, postFullname : String, postSource: PostRepository.PostSource) : DisplayPostVM {
            return DisplayPostVM(postRepo, commentRepo, listingRepo, networkUtil, subredditName, postFullname, postSource)
        }
    }

    private val TAG = "DISPLAYPOST_VM"
    private val validImageEndings = listOf("jpg", "png", "gif")

    private val _postLiveData = MediatorLiveData<PostModel> ()
    private val _commentListLiveData = MediatorLiveData<List<CommentModel>> ()
    private val _navigationLiveData = SingleLiveData<PostNavigationData> ()
    private val _refreshingLiveData = MutableLiveData<Boolean> ()
    private val _errorLiveData = MutableLiveData<PostExceptionData> ()

    val postLiveData : LiveData<PostModel> =_postLiveData
    val commentListLiveData : LiveData<List<CommentModel>> = _commentListLiveData
    val postNavigationLiveData : LiveData<PostNavigationData> = _navigationLiveData
    val refreshingLiveData : LiveData<Boolean> = _refreshingLiveData
    val errorLiveData : LiveData<PostExceptionData> = _errorLiveData

    private var retrievalInProgress = true

    init {
        observeLiveData()
        refreshData()
    }

    /**
     * Add sources and listeners to all local livedata
     */
    private fun observeLiveData() {
        _postLiveData.addSource<PostModel>(postRepo.getPost(postFullname)) { post ->
            post?.let {
                _postLiveData.postValue(it)

                if (post.commentCount == 0) {
                    _errorLiveData.postValue(PostExceptionData.NoComments)
                } else {
                    _errorLiveData.postValue(null)
                }
            }
        }

        _commentListLiveData.addSource(commentRepo.getComments(postFullname)) { nullableComments ->
            nullableComments?.let { comments ->
                if (!retrievalInProgress) {
                    _commentListLiveData.postValue(comments)
                    _refreshingLiveData.postValue(false)
                }
            }
        }
    }

    override fun refreshData() {
        if (networkUtil.checkConnection()) {
            _refreshingLiveData.postValue(true)

            GlobalScope.launch {
                val commentJob = launch { retrieveMoreComments(true) }
                val postJob = launch {
                    postRepo.retrievePost(subName, postFullname, postSource) { exception: RelicRequestError ->
                        publishException(exception)
                    }
                }

                try {
                    joinAll(commentJob, postJob)
                    retrievalInProgress = false
                }
                catch (e : Exception) {
                    publishException(e)
                    retrievalInProgress = false
                }
            }
        }
        else {
            _refreshingLiveData.postValue(false)
            publishException(PostExceptionData.NetworkUnavailable)
            retrievalInProgress = false
        }
    }

    override fun retrieveMoreComments(refresh: Boolean) {
        // TODO check if there is connection
        // retrieves post and comments from network
        GlobalScope.launch {
            if (refresh) {
                _refreshingLiveData.postValue(true)
                commentRepo.clearAllCommentsFromSource(postFullname)
            }
            commentRepo.retrieveComments(subName, postFullname, refresh)
        }
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

    /**
     * converts exceptions we handle into exceptions we've defined in the contract and posts
     * it to the livedata to let the view display it
     */
    private fun publishException(exception : Exception) {
        val viewException : PostExceptionData = when (exception) {
            is RelicRequestError -> PostExceptionData.NetworkUnavailable
            else -> PostExceptionData.UnexpectedException
        }

        publishException(viewException)
    }

    private fun publishException(viewException : PostExceptionData) {
        _refreshingLiveData.postValue(false)
        _errorLiveData.postValue(viewException)
    }

    //  region view action delegate

    override fun onExpandReplies(commentId: String, expanded : Boolean) {
//        val commentModel = _commentListLiveData.value!![position]
//
//        if (expanded) {
//            removeReplies(position)
//        } else {
//            val commentSource = commentRepo.getReplies(commentModel.id)
//            _commentListLiveData.addSource(commentSource) { replies ->
//                replies?.let {
//                    if (it.isNotEmpty()) {
//                        insertReplies(position, it)
//                    } else {
//                        // TODO retrieve comments from server if replies are not loaded
//                        commentRepo.retrieveCommentChildren(commentModel)
//                    }
//                    // remove this as a source since this is a one off to retrieve replies
//                    _commentListLiveData.removeSource(commentSource)
//                }
//            }
//        }

        val commentPosition = _commentListLiveData.value!!.indexOfFirst { it.fullName == commentId }
        val commentModel = _commentListLiveData.value!![commentPosition]

        if (expanded) {
            removeReplies(commentPosition)
        } else {
            val commentSource = commentRepo.getReplies(commentModel.fullName)
            _commentListLiveData.addSource(commentSource) { replies ->
                replies?.let {
                    if (it.isNotEmpty()) {
                        insertReplies(commentPosition, it)
                    } else {
                        GlobalScope.launch {
                            // TODO retrieve comments from server if replies are not loaded
                            commentRepo.retrieveCommentChildren(commentModel)
                        }
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

    override fun onLinkPressed() {
        _navigationLiveData.value = when (determineType()) {
            is DisplayPostType.Image -> PostNavigationData.ToImage(_postLiveData.value!!.url)
            is DisplayPostType.Link -> PostNavigationData.ToURL(_postLiveData.value!!.url)
            else -> null
        }
        _navigationLiveData.value = null
    }

    override fun onReplyPressed() {
        _navigationLiveData.value = PostNavigationData.ToReply(postFullname)
        _navigationLiveData.value = null
    }

    override fun onUserPressed(commentModel: CommentModel) {
        _navigationLiveData.value = PostNavigationData.ToUserPreview(commentModel.author)
    }

    // endregion view action delegate

    // region helpers

    fun determineType(): DisplayPostType? {
        var type : DisplayPostType? = null

        _postLiveData.value?.let {
            // check url ending to see if it's an image
            if (it.url != null) {
                val lastThree = it.url.substring(it.url.length - 3)
                if (validImageEndings.contains(lastThree)) {
                    type = DisplayPostType.Image
                }
                else if (!it.self) {
                    type = DisplayPostType.Link
                }
            }

        }

        return type
    }

    // endregion helpers

}
