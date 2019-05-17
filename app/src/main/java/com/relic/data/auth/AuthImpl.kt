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
import com.relic.data.Auth
import com.relic.network.VolleyQueue
import com.relic.presentation.callbacks.AuthenticationCallback

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

import java.util.Calendar
import java.util.Date
import java.util.HashMap

/**
 * Singleton instance of the authenticator because we should be able to
 */
class AuthImpl (private val appContext: Context) : Auth {
    private val TAG = "AUTHENTICATOR"

    private val preference: String
    private val redirectCode: String

    // keys for shared preferences
    private val KEY_USERNAME = "PREF_USERNAME"
    private val tokenKey: String
    private val refreshTokenKey: String

    private val requestQueue: RequestQueue
    private var lastRefresh: Date? = null

    val url: String
        get() = (AuthConstants.BASE + "client_id=" + appContext.getString(R.string.client_id)
            + "&response_type=" + AuthConstants.RESPONSE_TYPE
            + "&state=" + AuthConstants.STATE
            + "&redirect_uri=" + AuthConstants.REDIRECT
            + "&duration=" + AuthConstants.DURATION
            + "&scope=" + AuthConstants.SCOPE)

    /**
     * checks if the user is currently signed in by checking shared preferences
     * @return whether the user is signed in
     */
    private val isAuthenticated: Boolean
        get() = appContext.getSharedPreferences(preference, Context.MODE_PRIVATE).contains(tokenKey)

    // get the current user from shared preferences
    val user: String?
        get() {
            val prefEditor = appContext.getSharedPreferences(KEY_USERNAME, Context.MODE_PRIVATE)
            return prefEditor.getString(KEY_USERNAME, null)
        }


    init {
        requestQueue = VolleyQueue.get(appContext)

        // retrieve the strings from res
        appContext.resources.apply {
            preference = getString(R.string.AUTH_PREF)
            tokenKey = getString(R.string.TOKEN_KEY)
            refreshTokenKey = getString(R.string.REFRESH_TOKEN_KEY)
            redirectCode = getString(R.string.REDIRECT_CODE)
        }

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
    fun retrieveAccessToken(redirectUrl: String, callback: AuthenticationCallback) {
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
        requestQueue.add(RedditGetTokenRequest(Request.Method.POST, AuthConstants.ACCESS_TOKEN_URI,
            Response.Listener { response ->
                Log.d(TAG, response)
                saveReturn(response, callback)
            },
            Response.ErrorListener { error -> Log.d(TAG, "Error retrieving access token through reddit $error") })
        )
    }


    /**
     * gets a new current access token using the refresh token
     */
    override fun refreshToken(callback: AuthenticationCallback) {
        requestQueue.add(
            RedditGetRefreshRequest(
                Request.Method.POST,
                AuthConstants.ACCESS_TOKEN_URI,
                Response.Listener { response ->
                    Log.d(TAG, "Token refreshed$response")
                    saveReturn(response, callback)

                    // set time of refresh
                },
                Response.ErrorListener { error -> Log.d(TAG, "Token failed to refresh = $error") }
            )
        )
    }

    fun initializeUser(username: String) {
        // stores the current user in shared preferences
        val prefEditor = appContext
            .getSharedPreferences(KEY_USERNAME, Context.MODE_PRIVATE).edit()

        prefEditor.putString(KEY_USERNAME, username).apply()
    }

    /**
     * parses the successful auth response to store the oauth and refresh token in the shared
     * preferences. Then refreshes the token to get the permanent token
     * @param response
     */
    private fun saveReturn(response: String, callback: AuthenticationCallback) {
        val parser = JSONParser()
        try {
            val data = parser.parse(response) as JSONObject
            Log.d(TAG, "" + data["scope"]!!.toString())

            // stores the token in shared preferences
            val prefEditor = appContext
                .getSharedPreferences("auth", Context.MODE_PRIVATE).edit()

            // checks if there is an refresh token to be stored
            if (data.containsKey(refreshTokenKey)) {
                prefEditor.putString(refreshTokenKey, data[refreshTokenKey] as String?).apply()
            }

            prefEditor.putString(tokenKey, data[tokenKey] as String?).apply()
            Log.d(TAG, "token saved! " + (data[tokenKey] as String?)!!)

            callback.onAuthenticated()

        } catch (e: ParseException) {
            // TODO remove toast code. It has no business being here
            Toast.makeText(appContext, "yikes", Toast.LENGTH_SHORT).show()
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
        errorListener: Response.ErrorListener
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

            val refreshToken = appContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString(refreshTokenKey, "DEFAULT")

            params["grant_type"] = refreshTokenKey
            params["refresh_token"] = refreshToken!!

            return params
        }
    }

    companion object {

        fun checkToken(appContext: Context): String? {
            // retrieve the auth token shared preferences
            val authKey = appContext.resources.getString(R.string.AUTH_PREF)
            val tokenKey = appContext.resources.getString(R.string.TOKEN_KEY)
            return appContext
                .getSharedPreferences(authKey, Context.MODE_PRIVATE)
                .getString(tokenKey, "")
        }
    }

}
