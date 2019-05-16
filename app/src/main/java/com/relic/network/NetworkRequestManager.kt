package com.relic.network

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.relic.R
import com.relic.network.request.RelicOAuthRequest
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Abstraction for all network requests
 */
class NetworkRequestManager (
    val applicationContext: Context
) {

    // TODO move this to be injected
    private val volleyQueue: RequestQueue = VolleyAccessor.getInstance(applicationContext).requestQueue

    fun processRequest(relicRequest : RelicOAuthRequest) {
        volleyQueue.add(relicRequest)
    }

    @Throws(VolleyError::class)
    suspend fun processRequest (
        method: Int,
        url: String,
        authToken: String? = null,
        data: MutableMap<String, String>? = null
    ) : String = suspendCoroutine { cont ->

        val relicRequest = RelicOAuthRequest(
            method, url,
            Response.Listener { response: String ->
                cont.resumeWith(Result.success(response))
            },
            Response.ErrorListener { e: VolleyError ->
                cont.resumeWithException(e)

            },
            authToken ?: checkToken(),
            data
        )

        volleyQueue.add(relicRequest)
    }

    // get the oauth token from the app's shared preferences
    private fun checkToken(): String {
        // retrieve the auth token shared preferences
        val authKey = applicationContext.resources.getString(R.string.AUTH_PREF)
        val tokenKey = applicationContext.resources.getString(R.string.TOKEN_KEY)
        return applicationContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
            .getString(tokenKey, "DEFAULT") ?: ""
    }
}