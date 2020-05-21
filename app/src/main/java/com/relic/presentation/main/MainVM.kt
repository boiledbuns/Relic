package com.relic.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.relic.data.Auth
import com.relic.data.UserRepository
import com.relic.domain.models.AccountModel
import com.relic.domain.models.UserModel
import com.relic.interactor.Contract
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.callbacks.AuthenticationCallback
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.util.RelicEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class MainVM(
    private val auth: Auth,
    private val userRepo: UserRepository,
    private val postInteractor: Contract.PostAdapterDelegate,
    private val subredditInteractor: Contract.SubAdapterDelegate
) : RelicViewModel(), MainContract.VM, CoroutineScope {

    class Factory @Inject constructor(
        private val auth: Auth,
        private val userRepo: UserRepository,
        private val postInteractor: Contract.PostAdapterDelegate,
        private val subredditInteractor: Contract.SubAdapterDelegate
    ) {
        fun create(): MainVM {
            return MainVM(auth, userRepo, postInteractor, subredditInteractor)
        }
    }

    private val _accountsLiveData = MediatorLiveData<List<AccountModel>>()
    private val _userChangedLiveData = MediatorLiveData<RelicEvent<UserModel>>()
    private val _navigationLiveData = MediatorLiveData<RelicEvent<NavigationData>>()

    val accountsLiveData: LiveData<List<AccountModel>> = _accountsLiveData
    val userChangedEventLiveData: LiveData<RelicEvent<UserModel>> = _userChangedLiveData
    val navigationLiveData : LiveData<RelicEvent<NavigationData>> = _navigationLiveData

    init {
        _accountsLiveData.addSource(userRepo.getAccounts()) { accounts ->
            _accountsLiveData.postValue(accounts)
        }
        _navigationLiveData.apply {
            addSource(postInteractor.navigationLiveData) { postValue(it) }
            addSource(subredditInteractor.navigationLiveData) { postValue(it) }
        }

        if (auth.isAuthenticated()) {
            launch(Dispatchers.Main) {
                auth.refreshToken(AuthenticationCallback {
                    Timber.d("Token refreshed")
                    retrieveUser()
                })
            }
        }
    }

    override fun onAccountSelected(name: String?) {
        launch(Dispatchers.Main) {
            if (name != null) {
                // account switch, we're in charge of refreshing info about the account
                // update the current account so we can retrieve the user associated with it
                userRepo.setCurrentAccount(name)
                // since we're switching the user, need to refresh the auth token
                auth.refreshToken(AuthenticationCallback {
                    retrieveUser()
                })
            } else {
                // username should already have been set, we just need to update livedata appropriately
                _userChangedLiveData.postValue(RelicEvent(userRepo.getCurrentUser()!!))
            }
        }
    }

    override fun isAuthenticated(): Boolean = auth.isAuthenticated()

    private fun retrieveUser() {
        launch(Dispatchers.Main) {
            // need to retrieve current user (to get the username) before retrieving the account
            userRepo.getCurrentUser()?.let { user ->
                Timber.d("user $user")
                userRepo.retrieveAccount(user.name)
            }
        }
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {}
}