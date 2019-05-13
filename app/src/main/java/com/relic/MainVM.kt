package com.relic

import android.arch.lifecycle.*
import android.util.Log
import com.relic.data.Authenticator
import com.relic.data.UserRepository
import com.relic.data.models.UserModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainVM(
    val auth : Authenticator,
    val userRepo : UserRepository
) : MainContract.VM, ViewModel() {
    val TAG = "MAIN_VM"

    class Factory @Inject constructor(
        private val auth : Authenticator,
        private val userRepo : UserRepository
    ) {
        fun create () : MainVM {
            return MainVM(auth, userRepo)
        }
    }

    private val _userLiveData = MutableLiveData<UserModel>()
    val userLiveData : LiveData<UserModel> = _userLiveData

    init {

    }

    override fun onUserSelected() {
        GlobalScope.launch {
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