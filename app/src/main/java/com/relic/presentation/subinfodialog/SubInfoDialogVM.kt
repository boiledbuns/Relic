package com.relic.presentation.subinfodialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.SubRepository
import com.relic.domain.models.SubredditModel
import com.relic.presentation.base.RelicViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SubInfoDialogVM (
        private val subRepo: SubRepository,
        private val subredditName: String
) : RelicViewModel() {

    class Factory @Inject constructor(private val subRepository: SubRepository) {
        fun create(subredditName : String) : SubInfoDialogVM{
            return SubInfoDialogVM(subRepository, subredditName)
        }
    }

    private val _subredditLiveData = subRepo.getSingleSub(subredditName)
    val subredditLiveData : LiveData<SubredditModel> = _subredditLiveData

    private val _sideBarLiveData = MutableLiveData<String>()
    val sideBarLiveData : LiveData<String> = _sideBarLiveData

    init {
        launch (Dispatchers.Main) {
            subRepo.getSubGateway().retrieveAdditionalSubInfo(subredditName)

            val sidebar = subRepo.getSubGateway().retrieveSidebar(subredditName)
            _sideBarLiveData.postValue(sidebar)
        }
    }

    fun updateSubscriptionStatus(subscribed: Boolean) {
        // check if subreddit is currently pinned
    }

    fun pinSubreddit(pinned : Boolean) {
        launch(Dispatchers.Main) {
            subRepo.pinSubreddit(subredditName, true)
        }
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        Timber.e(e)
    }
}