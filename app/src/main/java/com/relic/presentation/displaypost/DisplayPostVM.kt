package com.relic.presentation.displaypost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.gateway.PostGateway
import com.relic.data.repository.NetworkException
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel
import com.relic.network.NetworkUtil
import com.relic.network.request.RelicRequestError
import com.relic.presentation.base.RelicViewModel
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class DisplayPostVM (
    private val postRepo : PostRepository,
    private val commentRepo: CommentRepository,
    private val listingRepo: ListingRepository,
    private val postGateway: PostGateway,
    private val networkUtil : NetworkUtil,
    private val subName: String,
    private val postFullname: String
): RelicViewModel(), DisplayPostContract.ViewModel {

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val commentRepo: CommentRepository,
        private val listingRepo: ListingRepository,
        private val networkUtil : NetworkUtil,
        private val postGateway: PostGateway
    ) {
        fun create(
            subredditName : String,
            postFullname : String
        ) : DisplayPostVM {
            return DisplayPostVM(postRepo, commentRepo, listingRepo, postGateway, networkUtil, subredditName, postFullname)
        }
    }

    private val _postLiveData = MediatorLiveData<PostModel> ()
    private val _commentListLiveData = MediatorLiveData<List<CommentModel>> ()
    private val _errorLiveData = MutableLiveData<PostErrorData> ()

    val postLiveData : LiveData<PostModel> =_postLiveData
    val commentListLiveData : LiveData<List<CommentModel>> = _commentListLiveData
    val errorLiveData : LiveData<PostErrorData> = _errorLiveData

    init {
        _postLiveData.addSource<PostModel>(postRepo.getPost(postFullname)) { post ->
            _postLiveData.postValue(post)
        }

        if (networkUtil.checkConnection()) {
            refreshData()
        } else {
            launch(Dispatchers.IO) {
                val localComments = commentRepo.getComments(postFullname)
                Timber.d(localComments.size.toString())
                _commentListLiveData.postValue(localComments)
            }

            publishException(PostErrorData.NetworkUnavailable)
        }
    }

    override fun refreshData() {
        if (networkUtil.checkConnection()) {
            launch(Dispatchers.Main) {
                val commentsAndPost = commentRepo.retrieveComments(subName, postFullname, refresh = true)


                // TODO when adding the preferences manager, check if user wants to save comments
                launch {
                    // remove previous comments for this post and stores new results
                    commentRepo.deleteComments(postFullname)
                    commentRepo.insertComments(commentsAndPost.comments)
                }

                _postLiveData.postValue(commentsAndPost.post)
                _commentListLiveData.postValue(commentsAndPost.comments)
            }
        }
        else {
            publishException(PostErrorData.NetworkUnavailable)
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
        val viewException : PostErrorData = when (exception) {
            is RelicRequestError -> PostErrorData.NetworkUnavailable
            else -> PostErrorData.UnexpectedException
        }

        publishException(viewException)
    }

    private fun publishException(viewException : PostErrorData) {
        _errorLiveData.postValue(viewException)
    }

    //  region view action delegate

    override fun onExpandReplies(comment: CommentModel, expanded : Boolean) {
        launch(Dispatchers.Main){
            val deferredParentPos = async { _commentListLiveData.value!!.indexOfFirst { it.fullName == comment.fullName } }

            if (expanded) {
                removeReplies(deferredParentPos.await())
            } else {

                val moreReplies = commentRepo.retrieveCommentChildren(postFullname, comment)

                _commentListLiveData.value?.let { commentList ->
                    val newList = commentList.toMutableList().apply {
                        val parentPos = deferredParentPos.await()
                        removeAt(parentPos)
                        addAll(parentPos, moreReplies)
                    }

                    _commentListLiveData.postValue(newList)
                }
            }
        }
    }

    // endregion view action delegate

    override fun handleException(context: CoroutineContext, e: Throwable) {
        val postE = when (e) {
            is NetworkException -> PostErrorData.NetworkUnavailable
            else -> PostErrorData.UnexpectedException
        }

        publishException(postE)
    }
}
