package com.relic.presentation.main

import androidx.lifecycle.*
import android.util.Log
import com.relic.data.Auth
import com.relic.data.auth.AuthImpl
import com.relic.data.UserRepository
import com.relic.domain.models.AccountModel
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.callbacks.AuthenticationCallback
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class MainVM(
    private val auth : Auth,
    private val userRepo : UserRepository
) : RelicViewModel(), MainContract.VM, CoroutineScope {

    class Factory @Inject constructor(
        private val auth : Auth,
        private val userRepo : UserRepository
    ) {
        fun create () : MainVM {
            return MainVM(auth, userRepo)
        }
    }

    private val _accountsLiveData = MediatorLiveData<List<AccountModel>>()
    private val _userLiveData = MediatorLiveData<UserModel>()

    val accountsLiveData : LiveData<List<AccountModel>> = _accountsLiveData
    val userLiveData : LiveData<UserModel> = _userLiveData

    init {
        launch(Dispatchers.Main) {
            auth.refreshToken(AuthenticationCallback {
                Log.d(TAG, "Token refreshed")
                retrieveUser()
            })
        }

        _accountsLiveData.addSource(userRepo.getAccounts()) { accounts ->
            _accountsLiveData.postValue(accounts)
        }
    }

    override fun onAccountSelected(name : String?) {
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
                _userLiveData.postValue(userRepo.getCurrentUser())
            }
        }
    }

    private fun retrieveUser() {
        launch(Dispatchers.Main) {
            // need to retrieve current user (to get the username) before retrieving the account
            userRepo.getCurrentUser()?.let { user ->
                Log.d(TAG, "user $user")
                _userLiveData.postValue(user)

                userRepo.retrieveAccount(user.name)
            }
        }
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {}
}