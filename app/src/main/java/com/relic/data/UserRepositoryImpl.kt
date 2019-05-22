package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import com.relic.data.deserializer.AccountDeserializerImpl
import com.relic.data.deserializer.Contract
import com.relic.data.deserializer.DeserializationException
import com.relic.data.deserializer.UserDeserializerImpl
import com.relic.data.models.AccountModel
import com.relic.data.models.UserModel
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

class UserRepositoryImpl (
    private val appContext: Context,
    private val requestManager: NetworkRequestManager
): UserRepository {

    private val ENDPOINT = "https://oauth.reddit.com"
    private val TAG = "USER_REPO"

    private val KEY_ACCOUNTS_DATA = "PREF_ACCOUNTS_DATA"
    private val KEY_CURR_ACCOUNT = "PREF_CURR_ACCOUNT"

    private val appDB = ApplicationDB.getDatabase(appContext)
    private val accountDao = appDB.accountDao

    private val userDeserializer = UserDeserializerImpl(appContext)
    private val accountDeserializer : Contract.AccountDeserializer = AccountDeserializerImpl(appContext)
    private val jsonParser: JSONParser = JSONParser()

    override suspend fun retrieveUsername(): String? {
        val selfEndpoint = "$ENDPOINT/api/v1/me"
        var username : String? = null

        withContext (Dispatchers.IO){
            try {
                // create the new request and submit it
                val response = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = selfEndpoint
                )

                val responseJson = jsonParser.parse(response) as JSONObject
                username = responseJson["name"] as String
            } catch (e: Exception) {
                throw transformException("Error retrieving self from $selfEndpoint", e)
            }
        }

        return username
    }

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
                throw transformException("retrieving user from $userEndpoint", e)
            }
        }

        return userModel
    }

    override suspend fun retrieveCurrentUser(): UserModel? {
        val name = appContext.getSharedPreferences(KEY_ACCOUNTS_DATA, Context.MODE_PRIVATE)
            .getString(KEY_CURR_ACCOUNT, null)

        return retrieveUser(name)
    }

    override suspend fun setCurrentAccount(username: String) {
        coroutineScope {
            appContext.getSharedPreferences(KEY_ACCOUNTS_DATA, Context.MODE_PRIVATE).let { sp ->
                sp.edit().putString(KEY_CURR_ACCOUNT, username)?.apply()
            }
        }
    }

    override fun getAccounts(): LiveData<List<AccountModel>> {
        return accountDao.getAccounts()
    }

    override suspend fun retrieveAccount(name : String) {
        val prefEndpoint = "$ENDPOINT/api/v1/me/prefs"
        coroutineScope {
            val retrieveResult = launch {
                val response = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = prefEndpoint
                )
                Log.d(TAG, response)

                val accountEntity = accountDeserializer.parseAccount(response).apply{
                    // need to manually specify name here
                    this.name = name
                }
                withContext(Dispatchers.IO) {
                    accountDao.insertAccount(accountEntity)
                }
            }

            try {
                retrieveResult.join()
            } catch (e : Exception) {
                throw transformException("retrieving account from endpoint $prefEndpoint", e)
            }
        }
    }

    /**
     * Transforms library specific exceptions into more generic exceptions tied to repo interface
     * A bit overkill, but I'd prefer to have finer control over exception structure
     */
    private fun transformException(method : String, e : Throwable) : UserRepoError {
        Log.d(TAG, e.toString())
        return when (e) {
            is VolleyError-> UserRepoError.Retrieval(method, e)
            is DeserializationException -> UserRepoError.Deserialization(method, e)
            else -> UserRepoError.Unknown(e)
        }
    }
}