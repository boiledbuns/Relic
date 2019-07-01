package com.relic.data.auth

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log

import com.android.volley.Request
import com.relic.R
import com.relic.data.*
import com.relic.persistence.entities.TokenStoreEntity
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.relic.persistence.ApplicationDB
import com.relic.presentation.callbacks.AuthenticationCallback
import dagger.Reusable
import kotlinx.coroutines.*

import java.util.Calendar
import java.util.Date
import java.util.HashMap
import javax.inject.Inject

@Reusable
class AuthImpl @Inject constructor(
    private val appContext: Application,
    private val requestManager: NetworkRequestManager,
    private val userRepo : UserRepository,
    private val appDB : ApplicationDB,
    private val authDeserializer : Auth.Deserializer
) : Auth {
    private val TAG = "AUTHENTICATOR"

    // TODO move to interface after refactoring account repo keys for data in response
    private val preference = appContext.resources.getString(R.string.AUTH_PREF)
    private val redirectCode= appContext.resources.getString(R.string.REDIRECT_CODE)
    private val KEY_ACCOUNTS_DATA= "PREF_ACCOUNTS_DATA"
    private val KEY_CURR_ACCOUNT= "PREF_CURR_ACCOUNT"

    // keys for shared preferences
    private val refreshTokenKey= appContext.resources.getString(R.string.REFRESH_TOKEN_KEY)

    private var lastRefresh: Date? = null

    private val spAccountLiveData = MutableLiveData<String?>()
    private val listener : SharedPreferences.OnSharedPreferenceChangeListener by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key == KEY_CURR_ACCOUNT) {
                spAccountLiveData.postValue(sp.getString(KEY_CURR_ACCOUNT, null))
            }
        }
    }

    override val url = (AuthConstants.BASE + "client_id=" + appContext.getString(R.string.client_id)
            + "&response_type=" + AuthConstants.RESPONSE_TYPE
            + "&state=" + AuthConstants.STATE
            + "&redirect_uri=" + AuthConstants.REDIRECT
            + "&duration=" + AuthConstants.DURATION
            + "&scope=" + AuthConstants.SCOPE)

    init {
        // checks if the user is currently signed in by checking shared preferences
        val isAuthenticated = appContext.getSharedPreferences(KEY_ACCOUNTS_DATA, Context.MODE_PRIVATE).contains(KEY_CURR_ACCOUNT)

        if (isAuthenticated) {
            // refresh the auth token
            // move the date change to the refresh method
            Log.d(TAG, "Current date/time: " + Calendar.getInstance().time)
            lastRefresh = Calendar.getInstance().time
        }
    }

    /**
     * Callback used to parse the url after the user has authenticated through the reddit auth page.
     * Retrieves the code value and uses it to obtain the real auth token
     * @param redirectUrl url with params to parse
     */
    override suspend fun retrieveAccessToken(redirectUrl: String, callback: AuthenticationCallback) {
        val queryStrings = redirectUrl.substring(AuthConstants.REDIRECT.length + 1)
        val queryPairs = queryStrings.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // parses the redirect to get the access token needed to retrieve the access token
        val queryMap = HashMap<String, String>()
        for (queryPair in queryPairs) {
            val mapping = queryPair.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            queryMap[mapping[0]] = mapping[1]
        }

        // stores the redirect "code" in shared preferences for easy access
        Log.d(TAG, queryMap.keys.toString() + " " + queryMap[redirectCode])
        appContext.getSharedPreferences(preference, Context.MODE_PRIVATE).edit()
            .putString(redirectCode, queryMap[redirectCode]).apply()

        val redirectCode = queryMap[redirectCode]!!

        // override headers to add custom credentials in client_secret:redirect_code format
        val headers = HashMap<String, String>().apply {
            // generate encoded credential string with client id and code from redirect
            val credentials = appContext.getString(R.string.client_id) + ":" + redirectCode
            val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            put("Authorization", auth)
        }

        val params = HashMap<String, String>().apply {
            put("grant_type", "authorization_code")
            put("code", redirectCode)
            put("redirect_uri", AuthConstants.REDIRECT)
        }

        try {
            val response = requestManager.processUnauthenticatedRequest(
                method = Request.Method.POST,
                url = AuthConstants.ACCESS_TOKEN_URI,
                data = params,
                headers = headers
            )

            Log.d(TAG, "successful response $response")
            // we should control how we store the token here
            // TODO refactor the account repo into its own class in this package since the two
            // are more closely related than user <-> account
            val responseData = authDeserializer.parseAuthResponse(response)
            val username = retrieveUserName(responseData.access)

            if (username != null) {
                withContext(Dispatchers.IO) {
                    appDB.tokenStoreDao.insertTokenStore(
                        TokenStoreEntity().apply {
                            accountName = username
                            access = responseData.access
                            refresh = responseData.refresh
                        }
                    )

                    userRepo.setCurrentAccount(username)
                }
            } // TODO fail if username is null

            callback.onAuthenticated()

        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve token", e) ?: e
        }
    }

    /**
     * gets a new current access token using the refresh token
     */
    override suspend fun refreshToken(callback: AuthenticationCallback) {
        // get the name of the current account
        val name : String = appContext
            .getSharedPreferences(KEY_ACCOUNTS_DATA, Context.MODE_PRIVATE)
            .getString(KEY_CURR_ACCOUNT, null) ?: throw Exception() // TODO replace with custom e

        val refreshToken = withContext(Dispatchers.IO) {
            appDB.tokenStoreDao.getTokenStore(name).refresh
        }

        val code: String? = appContext.getSharedPreferences(preference, Context.MODE_PRIVATE)
            .getString(redirectCode, "DEFAULT")

        // create a new header map and add the right headers to it
        val headers = HashMap<String, String>().apply {
            // generate encoded credential string with client id and code from redirect
            val credentials = appContext.getString(R.string.client_id) + ":" + code
            val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            put("Authorization", auth)
        }

        val params = HashMap<String, String>().apply {
            put("grant_type", refreshTokenKey)
            put("refresh_token", refreshToken)
        }

        Log.d(TAG, "refresh token $refreshToken")
        try {
            val response = requestManager.processUnauthenticatedRequest(
                method = Request.Method.POST,
                url = AuthConstants.ACCESS_TOKEN_URI,
                data = params,
                headers = headers
            )

            val refreshData = authDeserializer.parseRefreshResponse(response)

            withContext(Dispatchers.IO) {
                appDB.tokenStoreDao.updateAccessToken(name, refreshData.access)
                callback.onAuthenticated()
            }

        } catch (e: Exception) {
            throw DomainTransfer.handleException("refresh token", e) ?: e
        }
    }

    private suspend fun retrieveUserName(accessToken : String) : String? {
        val selfEndpoint = "https://oauth.reddit.com/api/v1/me"

        return withContext (Dispatchers.IO){
            try { // create the new request and submit it
                val response = requestManager.processRequest(
                    method = RelicOAuthRequest.GET,
                    url = selfEndpoint,
                    authToken = accessToken
                )

                authDeserializer.parseGetUsernameResponse(response)
            } catch (e: Exception) {
                throw DomainTransfer.handleException("retrieve username", e) ?: e
            }
        }
    }

    /**
     * need to keep strong reference to prevent gc
     * Should be subscribed to in main vm
     */
    override fun getCurrentAccountName(): LiveData<String?> {
        return spAccountLiveData
    }
}
