package com.relic

import android.arch.lifecycle.*
import android.util.Log
import com.relic.data.Authenticator
import com.relic.data.UserRepository
import com.relic.data.models.AccountModel
import com.relic.data.models.UserModel
import com.relic.data.repository.RepoError
import kotlinx.coroutines.*
import java.lang.Exception
import javax.inject.Inject

class MainVM(
    private val auth : Authenticator,
    private val userRepo : UserRepository
) : MainContract.VM, CoroutineScope, ViewModel() {
    val TAG = "MAIN_VM"

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, e ->
        // TODO handle exception
        Log.d(TAG, "caught exception $e")
    }

    class Factory @Inject constructor(
        private val auth : Authenticator,
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
            // TODO convert callback to suspend
            auth.refreshToken {  }

            userRepo.getCurrentAccount()?.let { username ->
                // retrieve current user
                val user = userRepo.retrieveUser(username)
                _userLiveData.postValue(user)
                Log.d(TAG, "user $user")

            user?.let {
                    userRepo.retrieveAccount(it.name)
                }
            }
        }
    }

    override fun onUserSelected() {
        launch(Dispatchers.Main) {
            val name = userRepo.getCurrentAccount()
            if (name == null) {
                // no account currently selected
            } else {
                val user = userRepo.retrieveUser(name)
                _userLiveData.postValue(user)
            }
        }
    }
}