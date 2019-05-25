package com.relic.data

import android.arch.lifecycle.LiveData
import com.relic.presentation.callbacks.AuthenticationCallback

interface Auth {
    suspend fun retrieveAccessToken(redirectUrl: String, callback: AuthenticationCallback)
    suspend fun refreshToken(callback: AuthenticationCallback)

    fun getCurrentAccountName() : LiveData<String?>
}

data class AuthResponseData(
    val access : String,
    val refresh : String
)

data class RefreshResponseData(
    val access : String
)