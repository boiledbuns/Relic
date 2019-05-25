package com.relic

import android.arch.lifecycle.*
import android.util.Log
import com.relic.data.auth.AuthImpl
import com.relic.data.UserRepository
import com.relic.data.models.AccountModel
import com.relic.data.models.UserModel
import com.relic.presentation.callbacks.AuthenticationCallback
import kotlinx.coroutines.*
import javax.inject.Inject

class MainVM(
    private val auth : AuthImpl,
    private val userRepo : UserRepository
) : MainContract.VM, CoroutineScope, ViewModel() {
    val TAG = "MAIN_VM"

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, e ->
        // TODO handle exception
        Log.d(TAG, "caught exception $e")
    }

    class Factory @Inject constructor(
        private val auth : AuthImpl,
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
            // update the current account so we can retrieve the user associated with it
            name?.let { userRepo.setCurrentAccount(name) }
            retrieveUser()
        }
    }

    private fun retrieveUser() {
        launch(Dispatchers.Main) {
            // need to retrieve current user (to get the username) before retrieving the account
            userRepo.retrieveCurrentUser()?.let { user ->
                Log.d(TAG, "user $user")
                _userLiveData.postValue(user)

                userRepo.retrieveAccount(user.name)
            }
        }
    }
}