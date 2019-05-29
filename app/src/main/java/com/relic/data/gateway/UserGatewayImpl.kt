package com.relic.data.gateway

import android.content.Context
import android.os.AsyncTask
import android.util.Log

import com.relic.R
import com.relic.data.DomainTransfer
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

class UserGatewayImpl(context: Context, private val requestManager: NetworkRequestManager) : UserGateway {
    private val ENDPOINT = "https://oauth.reddit.com/"

    override suspend fun getUser(username: String) {
        val endpoint = "$ENDPOINT/user/$username/about"
        try {
            val response = requestManager.processRequest(RelicOAuthRequest.GET, endpoint)
            parseUser(response)

        } catch (e: Exception) {
            throw DomainTransfer.handleException("refresh token", e) ?: e
        }
    }

    private fun parseUser(response: String) {
        Log.d(TAG, response)
        val parser = JSONParser()

        try {
            val full = (parser.parse(response) as JSONObject)["data"] as JSONObject?

            Log.d(TAG, full!!.keys.toString())
        } catch (e: ParseException) {
            Log.d(TAG, "Error parsing the response")
        }

    }

    companion object {
        var TAG = "USER_GATEWAY"
    }
}
