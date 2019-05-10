package com.relic.data

import android.content.Context
import android.util.Log
import com.relic.data.deserializer.UserDeserializerImpl
import com.relic.data.models.UserModel
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

class UserRepositoryImpl (
    private val appContext: Context,
    private val requestManager: NetworkRequestManager
): UserRepository {

    companion object {
        private const val ENDPOINT = "https://oauth.reddit.com"
        private const val TAG = "USER_REPO"
    }

    private val userDeserializer = UserDeserializerImpl(appContext)
    private val jsonParser: JSONParser = JSONParser()

    override suspend fun retrieveUser(username: String): UserModel? {
        val userEndpoint = "$ENDPOINT/user/$username/about"
        val trophiesEndpoint = "$ENDPOINT/api/v1/user/$username/trophies"

        var userModel : UserModel? = null

        coroutineScope {
            try {
                // create the new request and submit it
                val userResponse = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = userEndpoint
                )

                // create the new request and submit it
                val trophiesResponse = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = trophiesEndpoint
                )

                Log.d(TAG, "more posts $userResponse")
                Log.d(TAG, "trophies $trophiesResponse")

                userModel = userDeserializer.parseUser(userResponse, trophiesResponse)

            } catch (e: Exception) {
                Log.d(TAG, "Error retrieving user from ($userEndpoint) " + e.message)
            }
        }

        return userModel
    }

    override suspend fun retrieveSelf(): String? {
        val selfEndpoint = "$ENDPOINT/api/v1/me"
        var username : String? = null

        runBlocking {
            try {
                // create the new request and submit it
                val response = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = selfEndpoint
                )

                val responseJson = jsonParser.parse(response) as JSONObject
                username = responseJson["name"] as String

            } catch (e: Exception) {
                Log.d(TAG, "Error retrieving user from ($selfEndpoint) " + e.message)
            }
        }

        return username
    }

}