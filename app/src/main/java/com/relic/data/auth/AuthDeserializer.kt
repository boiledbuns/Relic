package com.relic.data.auth

import android.content.Context
import com.relic.data.AuthResponseData
import com.relic.data.RefreshResponseData
import com.relic.presentation.callbacks.AuthenticationCallback
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

class AuthDeserializer(
    val appContext : Context
) {
    private val TAG = "AUTH_DESERIALIZER"

    val parser = JSONParser()

    /**
     * parses the auth response to get the access and refresh token
     * Then refreshes the token to get the permanent token
     * @param response
     */
    fun parseAuthResponse(response: String) : AuthResponseData {
        val data = parser.parse(response) as JSONObject

        val accessToken = data[AuthConstants.ATOKEN_KEY] as String
        val refreshToken = data[AuthConstants.RTOKEN_KEY] as String

        return AuthResponseData(accessToken, refreshToken)
    }

    fun parseRefreshResponse(response: String) : RefreshResponseData {
        val data = parser.parse(response) as JSONObject
        val accessToken = data[AuthConstants.ATOKEN_KEY] as String

        return RefreshResponseData(accessToken)
    }

    fun parseGetUsernameResponse(response: String) : String? {
        val responseJson = parser.parse(response) as JSONObject
        return responseJson["name"] as String
    }
}