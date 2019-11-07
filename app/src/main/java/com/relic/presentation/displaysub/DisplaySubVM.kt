package com.relic.presentation.displaysub

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.SortScope
import com.relic.data.SortType
import com.relic.data.SubRepository
import com.relic.data.repository.NetworkException
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.network.NetworkUtil
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.main.RelicError
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

open class DisplaySubVM (
    private val postSource: PostSource,
    private val subRepo: SubRepository,
    private val postRepo: PostRepository,
    private val postInteractor :  DisplaySubContract.PostAdapterDelegate,
    private val listingRepo : ListingRepository,
    private val networkUtil : NetworkUtil
) : RelicViewModel(), DisplaySubContract.ViewModel, DisplaySubContract.PostAdapterDelegate by postInteractor {

    class Factory @Inject constructor(
        private val subRepo: SubRepository,
        private val postRepo : PostRepository,
        private val postInteractor : DisplaySubContract.PostAdapterDelegate,
        private val listingRepo : ListingRepository,
        private val networkUtil : NetworkUtil
    ) {
        fun create (postSource : PostSource) : DisplaySubVM {
            return DisplaySubVM(postSource, subRepo, postRepo, postInteractor, listingRepo, networkUtil)
        }
    }

    private var currentSortingType = SortType.DEFAULT
    private var currentSortingScope = SortScope.NONE
    private var subPostAfter : String? = null

    private var retrievalInProgress = true

    private val _subredditMediator = MediatorLiveData<SubredditModel>()
    private val _postListMediator= MediatorLiveData<List<PostModel>> ()
    private val _subInfoLiveData = MutableLiveData<DisplaySubInfoData>()
    private val _refreshLiveData = MutableLiveData<Boolean>()
    private val _errorLiveData = SingleLiveData<RelicError>()

    val subredditLiveData : LiveData<SubredditModel> = _subredditMediator
    val postListLiveData : LiveData<List<PostModel>> = _postListMediator
    val subInfoLiveData : LiveData<DisplaySubInfoData> = _subInfoLiveData
    val refreshLiveData : LiveData<Boolean> = _refreshLiveData
    val errorLiveData : LiveData<RelicError> = _errorLiveData

    init {
        when (postSource) {
            is PostSource.Subreddit -> {
                _subredditMediator.addSource(subRepo.getSingleSub(postSource.subredditName)) {
                    _subredditMediator.setValue(it)
                }
            }
            is PostSource.Frontpage -> {}
            is PostSource.All -> {}
        }
        
        // initial check for connection -> allows us to decide if we should use retrieve posts
        // from the network or just display what we have locally
        if (networkUtil.checkConnection()) {
            retrieveMorePosts(true)
            retrieveSubreddit()
        } else {
            // observe the list of posts stored locally
            _postListMediator.addSource(postRepo.getPosts(postSource)) { postModels ->
                _postListMediator.postValue(postModels)
            }

            launch {
                subPostAfter = listingRepo.getAfter(postSource)
            }

            //subRepo.getSubGateway().retrieveSubBanner(subName);
        }

        _subInfoLiveData.postValue(DisplaySubInfoData(
            sortingMethod = currentSortingType,
            sortingScope = currentSortingScope
        ))
    }

    private fun retrieveSubreddit() {
        val subName : String?  =  when (postSource) {
            is PostSource.Subreddit -> postSource.subredditName
            is PostSource.Frontpage -> ""
            else -> null
        }

        if (subName != null) {
            // TODO: STILL TESTING retrieve the banner image from the subreddit css
            launch(Dispatchers.Main) {
                subRepo.getSubGateway().apply {
                    retrieveAdditionalSubInfo(subName)
                    retrieveSidebar(subName)
                }

                val sub = subRepo.retrieveSingleSub(subName)
                _subredditMediator.postValue(sub)
                subRepo.insertSub(sub)
            }
        }
    }

    final override fun retrieveMorePosts(resetPosts: Boolean) {
        if (networkUtil.checkConnection()) {
            launch {
                if (resetPosts) {
                    // only indicate refreshing if connected to network
                    _refreshLiveData.postValue(true)
                    val listing = postRepo.retrieveSortedPosts(postSource, currentSortingType, currentSortingScope)
                    subPostAfter = listing.data.after

                    listing.data.children?.let { posts ->
                        _postListMediator.postValue(posts)
                        // TODO add preferences manager to let us check if user wants to store the loaded posts
                        postRepo.clearAllPostsFromSource(postSource)
                        postRepo.insertPosts(postSource, posts)
                    }

                    listingRepo.insertAfter(postSource, listing.data.after)
                } else {
                    subPostAfter?.let { currentAfter ->
                        val listing = postRepo.retrieveMorePosts(postSource, currentAfter)
                        subPostAfter = listing.data.after

                        listing.data.children?.let { posts ->
                            val newPosts = _postListMediator.value!!.toMutableList()
                            newPosts.addAll(posts)

                            _postListMediator.postValue(newPosts)
                            postRepo.insertPosts(postSource, posts)
                        }

                        listingRepo.insertAfter(postSource, listing.data.after)
                    }
                }

                retrievalInProgress = false
                _refreshLiveData.postValue(false)
            }
        }
        else {
            retrievalInProgress = false
            _errorLiveData.postValue(RelicError.NetworkUnavailable)
        }
    }

    /**
     * Called when the user has changed an aspect of the sorting method for this sub. Null values
     * for either the sortType or sortScope indicate no change
     * @param sortType : code corresponding to sort type
     * @param sortScope : code corresponding to sort scope
     */
    override fun changeSortingMethod(sortType: SortType?, sortScope: SortScope?) {
        // update the current sorting method and scope if it has changed
        sortType?.let { currentSortingType = it }
        sortScope?.let { currentSortingScope = it }

        launch(Dispatchers.Main) {
            // remove all posts from current db for this subreddit (triggers retrieval)
            postRepo.clearAllPostsFromSource(postSource)
            retrieveMorePosts(true)
            _subInfoLiveData.postValue(
                DisplaySubInfoData(sortingMethod = currentSortingType, sortingScope = currentSortingScope)
            )
        }
    }

    override fun updateSubStatus(subscribe: Boolean) {
        if (postSource is PostSource.Subreddit) {
            val subName = postSource.subredditName
            Timber.d("Changing to subscribed $subscribe")

            launch(Dispatchers.Main) {
                subRepo.getSubGateway().subscribe(subscribe, subName)
            }
        }
    }

    override fun handleException(context: CoroutineContext, e : Throwable) {
        val subE = when (e) {
            is NetworkException -> RelicError.NetworkUnavailable
            else -> RelicError.Unexpected
        }
        _errorLiveData.postValue(subE)

        Timber.e(e)
    }
}
