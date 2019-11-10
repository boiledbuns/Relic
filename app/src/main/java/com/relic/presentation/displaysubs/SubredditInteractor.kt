package com.relic.presentation.displaysubs

import androidx.lifecycle.LiveData
import com.relic.data.PostSource
import com.relic.data.gateway.SubGateway
import com.relic.domain.models.SubredditModel
import com.relic.presentation.displaysub.NavigationData
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubredditInteractor @Inject constructor(
    private val subGateway: SubGateway
): DisplaySubsContract.SubAdapterDelegate, CoroutineScope {
    override fun interact(subreddit: SubredditModel, subInteraction: DisplaySubsContract.SubInteraction) {
        when (subInteraction) {
            DisplaySubsContract.SubInteraction.Visit -> visit(subreddit)
            DisplaySubsContract.SubInteraction.Preview -> preview(subreddit)
            DisplaySubsContract.SubInteraction.Subscribe -> subscribe(subreddit)
        }
    }

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        Timber.e(e,  "caught exception")
    }

    private val _navigationLiveData = SingleLiveData<NavigationData>()
    override val navigationLiveData: LiveData<NavigationData> = _navigationLiveData

    fun visit(subItem: SubredditModel) {
        val navData = NavigationData.ToPostSource(PostSource.Subreddit(subItem.subName))
        _navigationLiveData.postValue(navData)
    }

    fun preview(subItem: SubredditModel) {
        val navData = NavigationData.PreviewPostSource(PostSource.Subreddit(subItem.subName))
        _navigationLiveData.postValue(navData)
    }

    fun subscribe(subreddit: SubredditModel) {
        launch { subGateway.subscribe(!subreddit.isSubscribed, subreddit.subName) }
    }
}