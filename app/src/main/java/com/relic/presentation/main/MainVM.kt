package com.relic.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.Auth
import com.relic.data.SubRepository
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
    private val subRepo: SubRepository,
    private val commentInteractor: Contract.CommentAdapterDelegate,
    private val postInteractor: Contract.PostAdapterDelegate,
    private val subredditInteractor: Contract.SubAdapterDelegate,
    private val userInteractor: Contract.UserAdapterDelegate
) : RelicViewModel(), MainContract.VM, CoroutineScope {

    class Factory @Inject constructor(
        private val auth: Auth,
        private val userRepo: UserRepository,
        private val subRepo: SubRepository,
        private val commentInteractor: Contract.CommentAdapterDelegate,
        private val postInteractor: Contract.PostAdapterDelegate,
        private val subredditInteractor: Contract.SubAdapterDelegate,
        private val userInteractor: Contract.UserAdapterDelegate
    ) {
        fun create(): MainVM {
            return MainVM(auth, userRepo, subRepo, commentInteractor, postInteractor, subredditInteractor, userInteractor)
        }
    }

    private val _accountsLiveData = MediatorLiveData<List<AccountModel>>()
    private val _userChangedEventLiveData = MediatorLiveData<RelicEvent<UserModel>>()
    private val _navigationLiveData = MediatorLiveData<RelicEvent<NavigationData>>()
    private val _initializationMessageLiveData = MutableLiveData<String>()

    val accountsLiveData: LiveData<List<AccountModel>> = _accountsLiveData
    val userChangedEventLiveData: LiveData<RelicEvent<UserModel>> = _userChangedEventLiveData
    val navigationEventLiveData: LiveData<RelicEvent<NavigationData>> = _navigationLiveData
    val initializationMessageLiveData: LiveData<String> = _initializationMessageLiveData

    init {
        _accountsLiveData.addSource(userRepo.getAccounts()) { accounts ->
            _accountsLiveData.postValue(accounts)
        }
        _navigationLiveData.apply {
            addSource(postInteractor.navigationLiveData) { postValue(it) }
            addSource(subredditInteractor.navigationLiveData) { postValue(it) }
            addSource(commentInteractor.navigationLiveData) { postValue(it) }
            addSource(userInteractor.navigationLiveData) { postValue(it) }
        }

        if (auth.isAuthenticated()) {
            launch(Dispatchers.Main) {
                auth.refreshToken(AuthenticationCallback { })
            }
        }
    }

    override fun onAccountSelected(name: String?) {
        launch(Dispatchers.Main) {
            if (name == null) {
                _initializationMessageLiveData.postValue("Please sit tight while we retrieve your subreddits :)")
                // first need to retrieve user subs since it's an expensive operation that should
                // be done sparingly
                val subs = subRepo.retrieveAllSubscribedSubs()
                subRepo.clearAndInsertSubs(subs)
                _initializationMessageLiveData.postValue(null)

                // if no name is supplied, it indicates a login
                // username should already have been set, we just need to post the event
                _userChangedEventLiveData.postValue(RelicEvent(userRepo.retrieveCurrentUser()!!))
            } else {
                // account switch, we're in charge of refreshing info about the account
                // update the current account so we can retrieve the user associated with it
                userRepo.setCurrentAccount(name)
                // since we're switching the user, need to refresh the auth token
                auth.refreshToken(AuthenticationCallback {
                    launch(Dispatchers.Main) {
                        // retrieve account details for new user
                        userRepo.retrieveAccount(name)
                    }
                })
            }
        }
    }

    override fun isAuthenticated(): Boolean = auth.isAuthenticated()

    override fun handleException(context: CoroutineContext, e: Throwable) {}
}