package com.relic.presentation.displaysub

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.relic.api.response.Listing
import com.relic.data.*
import com.relic.data.gateway.PostGateway
import com.relic.data.repository.NetworkException
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.network.NetworkUtil
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.helper.ImageHelper
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
    private val postGateway: PostGateway,
    private val networkUtil : NetworkUtil
) : RelicViewModel(), DisplaySubContract.ViewModel, DisplaySubContract.PostAdapterDelegate, DisplaySubContract.SearchVM {

    class Factory @Inject constructor(
        private val subRepo: SubRepository,
        private val postRepo : PostRepository,
        private val postGateway: PostGateway,
        private val networkUtil : NetworkUtil
    ) {
        fun create (postSource : PostSource) : DisplaySubVM {
            return DisplaySubVM(postSource, subRepo, postRepo, postGateway, networkUtil)
        }
    }

    private var currentSortingType = SortType.DEFAULT
    private var currentSortingScope = SortScope.NONE
    private var retrievalInProgress = true
    private var after : String? = null
    private var query : String? = null
    private var currentListing : Listing<PostModel>? = null

    private val _subredditMediator = MediatorLiveData<SubredditModel>()
    private val _postListMediator= MediatorLiveData<List<PostModel>> ()
    private val _navigationLiveData = SingleLiveData<SubNavigationData>()
    private val _subInfoLiveData = MutableLiveData<DisplaySubInfoData>()
    private val _refreshLiveData = MutableLiveData<Boolean>()
    private val _errorLiveData = SingleLiveData<RelicError>()
    private val _searchResults = MutableLiveData<List<PostModel>>()

    val subredditLiveData : LiveData<SubredditModel> = _subredditMediator
    val postListLiveData : LiveData<List<PostModel>> = _postListMediator
    val subNavigationLiveData : LiveData<SubNavigationData> = _navigationLiveData
    val subInfoLiveData : LiveData<DisplaySubInfoData> = _subInfoLiveData
    val refreshLiveData : LiveData<Boolean> = _refreshLiveData
    val errorLiveData : LiveData<RelicError> = _errorLiveData
    override val searchResults : LiveData<List<PostModel>> = _searchResults

    init {
        // initial check for connection -> allows us to decide if we should use retrieve posts
        // from the network or just display what we have locally
        if (networkUtil.checkConnection()) {
            retrieveMorePosts(true)
        } else {
            // observe the list of posts stored locally
            _postListMediator.addSource(postRepo.getPosts(postSource)) { postModels ->
                _postListMediator.postValue(postModels)
            }
        }

        when (postSource) {
            is PostSource.Subreddit -> initializeSubredditInformation(postSource.subredditName)
            is PostSource.Frontpage -> {}
            is PostSource.All -> {}
        }

        _subInfoLiveData.postValue(DisplaySubInfoData(
            sortingMethod = currentSortingType,
            sortingScope = currentSortingScope
        ))
    }

    private fun initializeSubredditInformation(subName : String ) {
        // TODO: STILL TESTING retrieve the banner image from the subreddit css
        launch(Dispatchers.Main) {
            subRepo.getSubGateway().apply {
                retrieveAdditionalSubInfo(subName)
                retrieveSidebar(subName)
            }
        }

        //subRepo.getSubGateway().retrieveSubBanner(subName);
        _subredditMediator.addSource(subRepo.getSingleSub(subName)) { newModel ->
            if (newModel == null) {
                Timber.d("No subreddit saved locally, retrieving from network")
                launch(Dispatchers.Main) { subRepo.retrieveSingleSub(subName) }
            } else {
                Timber.d("Subreddit loaded " + newModel.getBannerUrl())
                _subredditMediator.setValue(newModel)
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
                    currentListing = listing

                    listing.data.children?.let { posts ->
                        _postListMediator.postValue(posts)
                        // TODO add preferences manager to let us check if user wants to store the loaded posts
                        postRepo.clearAllPostsFromSource(postSource)
                        postRepo.insertPosts(postSource, posts)
                    }
                } else {
                    currentListing?.data?.after?.let { after ->
                        val listing = postRepo.retrieveMorePosts(postSource, after)
                        currentListing = listing

                        listing.data.children?.let { posts ->
                            val newPosts = _postListMediator.value!!.toMutableList()
                            newPosts.addAll(posts)

                            _postListMediator.postValue(newPosts)
                            postRepo.insertPosts(postSource, posts)
                        }
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
            Log.d(TAG, "Changing to subscribed $subscribe")

            launch(Dispatchers.Main) {
                subRepo.getSubGateway().subscribe(subscribe, subName)
            }
        }
    }

    // region view action delegate

    override fun visitPost(postFullname : String, subreddit : String) {
        launch(Dispatchers.Main) { postGateway.visitPost(postFullname) }
        _navigationLiveData.value = SubNavigationData.ToPost(postFullname, subreddit, postSource)
    }

    override fun voteOnPost(postFullname: String, voteValue: Int) {
        Log.d(TAG, "Voting on post " + postFullname + "value = " + voteValue)
        launch(Dispatchers.Main) { postGateway.voteOnPost(postFullname, voteValue) }
    }

    override fun savePost(postFullname: String, save: Boolean) {
        Log.d(TAG, "Saving on post " + postFullname + "save = " + save)
        launch(Dispatchers.Main) { postGateway.savePost(postFullname, save) }
    }

    override fun onLinkPressed(url: String) {
        val isImage = ImageHelper.isValidImage(url)

        val subNavigation : SubNavigationData = if (isImage) {
            SubNavigationData.ToImage(url)
        } else {
            SubNavigationData.ToExternal(url)
        }

        _navigationLiveData.value = subNavigation
    }

    override fun previewUser(username: String) {
        _navigationLiveData.value = SubNavigationData.ToUserPreview(username)
    }


    // region search vm
    override fun search(query : String) {
        this.query = query
        launch(Dispatchers.Main) {
            val results = when (postSource) {
                is PostSource.Subreddit -> {
                    postRepo.searchSubPosts(postSource.subredditName, query, true)
                }
                else -> {
                    null
                }
            }

            results?.let {
                after = it.after
                _searchResults.postValue(it.posts)
            }
        }
    }

    override fun retrieveMoreSearchResults() {
        val currQuery = query
        launch(Dispatchers.Main) {
            if (after != null && currQuery != null){
                val results = when (postSource) {
                    is PostSource.Subreddit -> {
                        postRepo.searchSubPosts(postSource.subredditName, currQuery, true, after)
                    }
                    else -> {
                        null
                    }
                }

                results?.let { moreResult ->
                    after = moreResult.after
                    // show appropriate message to user to indicate no more posts could be found
                    if (moreResult.posts.isEmpty()) {
                        _errorLiveData.postValue(NoResults)
                    } else {
                        val newPosts = ArrayList<PostModel>()
                        _searchResults.value?.let { newPosts.addAll(it) }
                        newPosts.addAll(moreResult.posts)

                        _searchResults.postValue(newPosts)
                    }
                }

            } else {
                Log.d(TAG, "No more posts available for this query")
            }
        }
    }

    // endregion search vm

    // endregion view action delegate

    override fun handleException(context: CoroutineContext, e : Throwable) {
        val subE = when (e) {
            is NetworkException -> RelicError.NetworkUnavailable
            else -> RelicError.Unexpected
        }
        _errorLiveData.postValue(subE)

        Log.e(TAG, "caught exception", e)
    }
}
