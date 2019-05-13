package com.relic.data

import com.relic.data.models.UserModel

interface UserRepository {
    suspend fun retrieveUser(username : String) : UserModel?

    suspend fun retrieveSelf() : String?

    // region user authenticated functions

    /**
     * adds an account to set of authenticated account
     */
    suspend fun addAuthenticatedAccount(username : String)

    /**
     * sets an account from the list of authenticated account as the current one
     */
    suspend fun setCurrentAccount(username : String)

    /**
     * gets the current account in use
     */
    fun getCurrentAccount() : String?

    // endregion user authenticated functions
}

sealed class UserRepoException : Exception() {
    object UserAlreadyAuthenticated : UserRepoException()

    object UserNotAuthenticated : UserRepoException()
}