package com.relic.network

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.relic.R
import com.relic.data.ApplicationDB
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Abstraction for all network requests
 */
class NetworkRequestManager (
    private val appContext: Context
) {
    private val KEY_ACCOUNTS_DATA = "PREF_ACCOUNTS_DATA"
    private val KEY_CURR_ACCOUNT = "PREF_CURR_ACCOUNT"

    // TODO move this to be injected
    private val volleyQueue: RequestQueue = VolleyAccessor.getInstance(appContext).requestQueue
    private val tokenStore = ApplicationDB.getDatabase(appContext).tokenStoreDao

    fun processRequest(relicRequest : RelicOAuthRequest) {
        volleyQueue.add(relicRequest)
    }

    /**
     * note that token should 99% be left empty (checked here) when calling process request
     * token field should only be filled in when processing a request for the first time
     * when a token is unavailable
     */
    @Throws(VolleyError::class)
    suspend fun processRequest (
        method: Int,
        url: String,
        authToken: String? = null,
        data: MutableMap<String, String>? = null
    ) : String {
        val token = authToken ?: checkToken()

        return suspendCoroutine { cont ->
            val relicRequest = RelicOAuthRequest(
                method, url,
                Response.Listener { response: String ->
                    cont.resumeWith(Result.success(response))
                },
                Response.ErrorListener { e: VolleyError ->
                    cont.resumeWithException(e)
                },
                token,
                data
            )

            volleyQueue.add(relicRequest)
        }
    }

    // get the oauth token from the app's shared preferences
    private suspend fun checkToken(): String {
        // get the name of the current account
        val name = appContext.getSharedPreferences(KEY_ACCOUNTS_DATA, Context.MODE_PRIVATE)
            .getString(KEY_CURR_ACCOUNT, null)

        return withContext(Dispatchers.IO) {
            tokenStore.getTokenStore(name).access
        }
    }
}