package com.relic.presentation.search.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.UserRepository
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.DisplaySearchContract
import com.relic.presentation.search.UserSearchOptions
import com.relic.presentation.search.UserSearchResults
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class UserSearchVM(
    private val userRepo: UserRepository
) : RelicViewModel(), DisplaySearchContract.UserSearchVM {

    class Factory @Inject constructor(
        private val userRepo: UserRepository
    ) {
        fun create() : UserSearchVM {
            return UserSearchVM(userRepo)
        }
    }

    private val _errorLiveData = MutableLiveData<RelicError?>()
    private val _searchResultsLiveData = MutableLiveData<UserSearchResults>()
    private val _navigationLiveData = SingleLiveData<NavigationData>()

    override val searchResultsLiveData: LiveData<UserSearchResults> = _searchResultsLiveData
    override val navigationLiveData: LiveData<NavigationData> = _navigationLiveData
    override val errorLiveData: LiveData<RelicError?> = _errorLiveData

    private var query : String = ""

    override fun updateQuery(newQuery: String) {
        query = newQuery
    }

    override fun search(newOptions: UserSearchOptions) {
        if (query.isEmpty()) {
            val searchResult = UserSearchResults(
                query = query,
                user = null
            )
            _searchResultsLiveData.postValue(searchResult)
        }
        else {
            launch(Dispatchers.Main) {
                val searchResult = UserSearchResults(
                        query = query,
                        user = userRepo.retrieveUser(query)
                )
                _searchResultsLiveData.postValue(searchResult)
            }
        }
    }

    override fun openUser(username: String?) {
        val newUsername = username?.let { it } ?: query
        _navigationLiveData.postValue(NavigationData.ToUser(newUsername))

    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
       Timber.e(e)
    }
}