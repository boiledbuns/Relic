package com.relic.interactor

import androidx.lifecycle.LiveData
import com.relic.data.PostSource
import com.relic.data.gateway.SubGateway
import com.relic.domain.models.SubredditModel
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.util.RelicEvent
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubredditInteractorImpl @Inject constructor(
    private val subGateway: SubGateway
) : Contract.SubAdapterDelegate, CoroutineScope {

    override fun interact(postSource: PostSource, subInteraction: SubInteraction) {
        when (subInteraction) {
            SubInteraction.Visit, SubInteraction.Preview -> {
                val navData = NavigationData.ToPostSource(postSource)
                _navigationLiveData.postValue(RelicEvent(navData))
            }
            is SubInteraction.Subscribe -> subscribe(subInteraction.subredditModel)
        }
    }

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        Timber.e(e, "caught exception")
    }

    private val _navigationLiveData = SingleLiveData<RelicEvent<NavigationData>>()
    override val navigationLiveData: LiveData<RelicEvent<NavigationData>> = _navigationLiveData

    fun subscribe(subreddit: SubredditModel) {
        launch { subGateway.subscribe(subreddit.isSubscribed, subreddit.subName) }
    }

    fun openSearch(subreddit: SubredditModel) {
        launch { subGateway.subscribe(subreddit.isSubscribed, subreddit.subName) }
    }
}