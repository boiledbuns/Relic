package com.relic.network

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.relic.persistence.ApplicationDB
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Abstraction for all network requests
 */
class NetworkRequestManager @Inject constructor(
    private val appContext : Context,
    appDB: ApplicationDB
) {
    val TAG = "NETWORK_REQUEST_MANAGER"

    private val KEY_ACCOUNTS_DATA = "PREF_ACCOUNTS_DATA"
    private val KEY_CURR_ACCOUNT = "PREF_CURR_ACCOUNT"

    // TODO move this to be injected
    private val volleyQueue: RequestQueue = VolleyAccessor.getInstance(appContext).requestQueue
    private val tokenStore = appDB.tokenStoreDao

    @Throws(VolleyError::class)
    suspend fun processUnauthenticatedRequest (
        method: Int,
        url: String,
        headers: MutableMap<String, String>? = null,
        data: MutableMap<String, String>? = null
    ) : String {
        return suspendCoroutine { cont ->
            val request = RelicOAuthRequest(
                method = method,
                url = url,
                listener = Response.Listener { response: String ->
                    cont.resumeWith(Result.success(response))
                },
                errorListener = Response.ErrorListener { e: VolleyError ->
                    cont.resumeWithException(e)
                },
                headers = headers,
                data = data
            )

            volleyQueue.add(request)
        }
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
        headers: MutableMap<String, String>? = null,
        data: MutableMap<String, String>? = null
    ) : String {

        val token = authToken ?: checkToken()

        return suspendCoroutine { cont ->
            val request = RelicOAuthRequest(
                method, url,
                Response.Listener { response: String ->
                    Timber.d("endpoint: %s \n response:  %s", url, response)
                    cont.resumeWith(Result.success(response))
                },
                Response.ErrorListener { e: VolleyError ->
                    cont.resumeWithException(e)
                },
                token,
                headers,
                data
            )

            volleyQueue.add(request)
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