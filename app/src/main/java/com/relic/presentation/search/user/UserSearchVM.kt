package com.relic.presentation.search.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.UserRepository
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.DisplaySearchContract
import com.relic.presentation.search.UserSearchOptions
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
    private val _searchResults = MutableLiveData<List<UserModel>>()

    override val errorLiveData: LiveData<RelicError?> = _errorLiveData
    override val searchResults: LiveData<List<UserModel>> = _searchResults

    private var query : String = ""

    override fun updateQuery(newQuery: String) {
        query = newQuery
    }

    override fun search(newOptions: UserSearchOptions) {
    }

    override fun retrieveMoreUsers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}