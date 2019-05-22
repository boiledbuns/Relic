package com.relic.data.auth

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.relic.R
import com.relic.data.ApplicationDB
import com.relic.data.Auth
import com.relic.data.UserRepository
import com.relic.data.UserRepositoryImpl
import com.relic.data.entities.TokenStoreEntity
import com.relic.network.NetworkRequestManager
import com.relic.network.VolleyQueue
import com.relic.network.request.RelicOAuthRequest
import com.relic.presentation.callbacks.AuthenticationCallback
import kotlinx.coroutines.*

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

import java.util.Calendar
import java.util.Date
import java.util.HashMap
import javax.inject.Inject

/**
 * Singleton instance of the authenticator because we should be able to
 */
class AuthImpl (
    private val appContext: Context
) : Auth {
    private val TAG = "AUTHENTICATOR"

    // TODO move to interface after refactoring account repo keys for data in response
    private val preference = appContext.resources.getString(R.string.AUTH_PREF)
    private val redirectCode= appContext.resources.getString(R.string.REDIRECT_CODE)
    private val KEY_ACCOUNTS_DATA= "PREF_ACCOUNTS_DATA"
    private val KEY_CURR_ACCOUNT= "PREF_CURR_ACCOUNT"

    // keys for shared preferences
    private val tokenKey= appContext.resources.getString(R.string.TOKEN_KEY)
    private val refreshTokenKey= appContext.resources.getString(R.string.REFRESH_TOKEN_KEY)

    private val requestQueue = VolleyQueue.get(appContext)
    private var lastRefresh: Date? = null
    private var authDeserializer = AuthDeserializer(appContext)

    private val appDB = ApplicationDB.getDatabase(appContext)
    // TODO convert to inject
    private val requestManager: NetworkRequestManager = NetworkRequestManager(appContext)
    private val userRepo : UserRepository = UserRepositoryImpl(appContext, requestManager)

    val url = (AuthConstants.BASE + "client_id=" + appContext.getString(R.string.client_id)
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

        // get the access and refresh token
        // TODO convert to use request manager to remove callbacks
        requestQueue.add(RedditGetTokenRequest(Request.Method.POST, AuthConstants.ACCESS_TOKEN_URI,
            Response.Listener { response ->
                Log.d(TAG, "successful response $response")
                // we should control how we store the token here
                // TODO refactor the account repo into its own class in this package since the two
                // are more closely related than user <-> account
                GlobalScope.launch (Dispatchers.Main + CoroutineExceptionHandler { _, e ->
                    Log.d(TAG, "failed to retrieve access token $e")
                }) {
                    Log.d(TAG, "test")
                    val responseData = authDeserializer.parseAuthResponse(response)
                    val username = retrieveUserName(responseData.access)

                    Log.d(TAG, "successful response $response")

                    if (username != null) {
                        withContext(Dispatchers.IO) {
                            appDB.tokenStoreDao.insertTokenStore(
                                TokenStoreEntity().apply {
                                    accountName = username
                                    access = responseData.access
                                    refresh = responseData.refresh
                                }
                            )

                            // TODO extract once account repo is refactored
                            // store the current account name in shared preferences
                            appContext.getSharedPreferences(KEY_ACCOUNTS_DATA, Context.MODE_PRIVATE).let { sp ->
                                sp.edit().putString(KEY_CURR_ACCOUNT, username)?.apply()
                            }
                        }
                    }

                    callback.onAuthenticated()
                }
            },
            Response.ErrorListener { error ->
                Log.d(TAG, "Error retrieving access token through reddit $error")
            })
        )

    }


    /**
     * gets a new current access token using the refresh token
     */
    override suspend fun refreshToken(callback: AuthenticationCallback) {
        coroutineScope {
            // get the name of the current account
            val name : String = appContext
                .getSharedPreferences(KEY_ACCOUNTS_DATA, Context.MODE_PRIVATE)
                .getString(KEY_CURR_ACCOUNT, null) ?: throw Exception()

            val refreshKey = withContext(Dispatchers.IO) {
                appDB.tokenStoreDao.getTokenStore(name).refresh
            }
            Log.d(TAG, "refresh key $refreshKey")
            requestQueue.add(
                RedditGetRefreshRequest(
                    Request.Method.POST,
                    AuthConstants.ACCESS_TOKEN_URI,
                    Response.Listener { response ->
                        GlobalScope.launch {
                            Log.d(TAG, "Token refreshed$response")
                            val refreshData = authDeserializer.parseRefreshResponse(response)

                            withContext(Dispatchers.IO) {
                                appDB.tokenStoreDao.updateAccessToken(name, refreshData.access)
                                callback.onAuthenticated()
                            }
                        }
                    },
                    Response.ErrorListener { error -> Log.d(TAG, "Token failed to refresh = $error") },
                    refreshKey
                )
            )
        }
    }

    /**
     * we need to retrieve
     */
    private suspend fun retrieveUserName(accessToken : String) : String? {
        val selfEndpoint = "https://oauth.reddit.com/api/v1/me"

        return withContext (Dispatchers.IO){
            // create the new request and submit it
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = selfEndpoint,
                authToken = accessToken
            )
            authDeserializer.parseGetUsernameResponse(response)
        }
    }

    internal inner class RedditGetTokenRequest (
        method: Int,
        url: String,
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) : StringRequest(method, url, listener, errorListener) {

        private val code: String? = appContext.getSharedPreferences(preference, Context.MODE_PRIVATE)
            .getString(redirectCode, "DEFAULT")

        // override headers to add custom credentials in client_secret:redirect_code format
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            // create a new header map and add the right headers to it
            val headers = HashMap<String, String>()

            // generate encoded credential string with client id and code from redirect
            val credentials = appContext.getString(R.string.client_id) + ":" + code
            val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            headers["Authorization"] = auth

            return headers
        }

        @Throws(AuthFailureError::class)
        public override fun getParams(): Map<String, String> {
            val params = HashMap<String, String>()

            params["grant_type"] = "authorization_code"
            params["code"] = code!!
            params["redirect_uri"] = AuthConstants.REDIRECT
            return params
        }
    }


    internal inner class RedditGetRefreshRequest (
        method: Int,
        url: String,
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener,
        private val refreshToken: String
    ) : StringRequest(method, url, listener, errorListener) {

        // override headers to add custom credentials in client_secret:redirect_code format
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            // create a new header map and add the right headers to it
            val headers = HashMap<String, String>()

            val code = appContext.getSharedPreferences(preference, Context.MODE_PRIVATE)
                .getString(redirectCode, "DEFAULT")
            // generate encoded credential string with client id and code from redirect
            val credentials = appContext.getString(R.string.client_id) + ":" + code
            val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            headers["Authorization"] = auth

            return headers
        }

        @Throws(AuthFailureError::class)
        public override fun getParams(): Map<String, String> {
            val params = HashMap<String, String>()

            params["grant_type"] = refreshTokenKey
            params["refresh_token"] = refreshToken

            return params
        }
    }
}
