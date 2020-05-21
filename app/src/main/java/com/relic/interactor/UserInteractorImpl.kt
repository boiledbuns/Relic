package com.relic.interactor

import androidx.lifecycle.LiveData
import com.relic.data.gateway.UserGateway
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.util.RelicEvent
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInteractorImpl @Inject constructor(
    private val userGateway: UserGateway
) : Contract.UserAdapterDelegate, CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        Timber.e(e, "caught exception")
    }

    private val _navigationLiveData = SingleLiveData<RelicEvent<NavigationData>>()
    override val navigationLiveData: LiveData<RelicEvent<NavigationData>> = _navigationLiveData

    override fun interact(interaction: UserInteraction) {
        when (interaction) {
            is UserInteraction.ViewUser -> {
                val navData = NavigationData.ToUser(interaction.username)
                _navigationLiveData.postValue(RelicEvent(navData))
            }
        }
    }
}