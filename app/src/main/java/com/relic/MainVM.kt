package com.relic

import android.arch.lifecycle.*
import com.relic.data.Authenticator
import com.relic.data.UserRepository
import com.relic.data.models.UserModel
import kotlinx.coroutines.*
import javax.inject.Inject

class MainVM(
    private val auth : Authenticator,
    private val userRepo : UserRepository
) : MainContract.VM, CoroutineScope, ViewModel() {
    val TAG = "MAIN_VM"

    override val coroutineContext = Dispatchers.Main + SupervisorJob()

    class Factory @Inject constructor(
        private val auth : Authenticator,
        private val userRepo : UserRepository
    ) {
        fun create () : MainVM {
            return MainVM(auth, userRepo)
        }
    }

    private val _userLiveData = MediatorLiveData<UserModel>()
    val userLiveData : LiveData<UserModel> = _userLiveData

    init {
        launch (Dispatchers.Main){
            // TODO store user locally
            userRepo.getCurrentAccount()?.let { username ->
                val user = userRepo.retrieveUser(username)
                _userLiveData.postValue(user)
            }
        }
    }

    override fun onUserSelected() {
        launch {
            val name = userRepo.getCurrentAccount()
            if (name == null) {
                // no account currently selected
            } else {
                userRepo.retrieveUser(name).apply {
                    _userLiveData.postValue(this)
                }
            }
        }
    }
}