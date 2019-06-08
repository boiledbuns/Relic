package com.relic.data.auth

import com.relic.data.Auth
import com.relic.data.AuthResponseData
import com.relic.data.RefreshResponseData
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import javax.inject.Inject

class AuthDeserializerImpl @Inject constructor() : Auth.Deserializer {
    private val TAG = "AUTH_DESERIALIZER"

    val parser = JSONParser()

    /**
     * parses the auth response to get the access and refresh token
     * Then refreshes the token to get the permanent token
     * @param response
     */
    override fun parseAuthResponse(response: String) : AuthResponseData {
        val data = parser.parse(response) as JSONObject

        val accessToken = data[AuthConstants.ATOKEN_KEY] as String
        val refreshToken = data[AuthConstants.RTOKEN_KEY] as String

        return AuthResponseData(accessToken, refreshToken)
    }

    override fun parseRefreshResponse(response: String) : RefreshResponseData {
        val data = parser.parse(response) as JSONObject
        val accessToken = data[AuthConstants.ATOKEN_KEY] as String

        return RefreshResponseData(accessToken)
    }

    override fun parseGetUsernameResponse(response: String) : String? {
        val responseJson = parser.parse(response) as JSONObject
        return responseJson["name"] as String
    }
}