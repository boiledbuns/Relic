package com.relic.data

import androidx.lifecycle.LiveData
import com.relic.api.response.Listing
import com.relic.domain.models.AccountModel
import com.relic.domain.models.UserModel
import java.lang.Exception

interface UserRepository {
    @Throws(UserRepoError::class)
    suspend fun retrieveUsername(): String?

    @Throws(UserRepoError::class)
    suspend fun retrieveUser(username : String) : UserModel?

    @Throws(UserRepoError::class)
    suspend fun getCurrentUser() : UserModel?

    suspend fun setCurrentAccount(username : String)

    @Throws(UserRepoError::class)
    suspend fun retrieveAccount(name : String)

    // search by username isn't actually supported by reddit api

    fun getAccounts() : LiveData<List<AccountModel>>
}

sealed class UserRepoError(message: String?, cause: Throwable?) : Exception(message, cause) {
    class Retrieval(message: String?, cause: Throwable?) : UserRepoError(message, cause)
    class Deserialization(message: String?, cause: Throwable?) : UserRepoError(message, cause)

    class Unknown(cause: Throwable?) : UserRepoError("Unknown error", cause)

    class UserNotAuthenticated(message: String?, cause: Throwable?) : UserRepoError(message, cause)
}