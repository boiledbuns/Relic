package com.relic.presentation.subinfodialog

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.relic.data.SubRepository
import com.relic.data.models.SubredditModel
import kotlinx.coroutines.*
import javax.inject.Inject

class SubInfoDialogVM (
        private val subRepo: SubRepository,
        private val subredditName: String
) : ViewModel(), CoroutineScope {
    val TAG = "SUBINFO_DIALOG_VM"

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, e ->
        // TODO handle exception
        Log.d(TAG, "caught exception $e")
    }

    class Factory @Inject constructor(private val subRepository: SubRepository) {
        fun create(subredditName : String) : SubInfoDialogVM{
            return SubInfoDialogVM(subRepository, subredditName)
        }
    }

    private val _subredditLiveData = subRepo.getSingleSub(subredditName)
    val subredditLiveData : LiveData<SubredditModel> = _subredditLiveData

    private val _sideBarLiveData = subRepo.getSubGateway().getSidebar(subredditName)
    val sideBarLiveData : LiveData<String> = _sideBarLiveData

    init {
        subRepo.getSubGateway().getAdditionalSubInfo(subredditName)
    }

    fun updateSubscriptionStatus(subscribed: Boolean) {
        // check if subreddit is currently pinned
    }

    fun pinSubreddit(pinned : Boolean) {
        launch(Dispatchers.Main) {
            subRepo.pinSubreddit(subredditName, true)
        }
    }
}