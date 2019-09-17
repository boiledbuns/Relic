package com.relic.data

import androidx.lifecycle.LiveData
import com.relic.presentation.callbacks.AuthenticationCallback

interface Auth {
    suspend fun retrieveAccessToken(redirectUrl: String, callback: AuthenticationCallback)
    suspend fun refreshToken(callback: AuthenticationCallback)

    fun getCurrentAccountName() : LiveData<String?>
    val url : String

    interface Deserializer {
        fun parseAuthResponse(response: String) : AuthResponseData
        fun parseRefreshResponse(response: String) : RefreshResponseData
        fun parseGetUsernameResponse(response: String) : String?
    }
}

data class AuthResponseData(
    val access : String,
    val refresh : String
)

data class RefreshResponseData(
    val access : String
)