package com.relic.network

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.relic.data.Auth
import com.relic.persistence.ApplicationDB
import com.relic.network.request.RelicOAuthRequest
import com.relic.presentation.callbacks.AuthenticationCallback
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Abstraction for all network requests
 */
class NetworkRequestManager @Inject constructor(
    private val appContext : Context,
    appDB: ApplicationDB
) {
    lateinit var authManager : Auth

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
//        val token = "35823412-qB2PuomlNACyVzBikhg1J53GYpA"

        return suspendCoroutine { cont ->
            val request = RelicOAuthRequest(
                method, url,
                Response.Listener { response: String ->
                    Timber.d("endpoint: %s \n response:  %s", url, response)
                    cont.resumeWith(Result.success(response))
                },
                Response.ErrorListener { e: VolleyError ->
                    // check if the failure is the result of an unauthenticated token
                    // try to refresh token and try request
                    if (e is AuthFailureError) {
                        GlobalScope.launch { handleAuthError(method, url, headers, data, cont) }
                    } else {
                        cont.resumeWithException(e)
                    }
                },
                token,
                headers,
                data
            )
            volleyQueue.add(request)
        }
    }

    // if a request fails because the token has expired, this function will
    // refresh the token and retry the request
    private suspend fun handleAuthError(
        method: Int,
        url: String,
        headers: MutableMap<String, String>? = null,
        data: MutableMap<String, String>? = null,
        cont: Continuation<String>
    ) {
        authManager.refreshToken(callback = AuthenticationCallback {
            GlobalScope.launch {
                val response = processRequest(method, url, null, headers, data)
                cont.resumeWith(Result.success(response))
            }
        })
    }

    // get the oauth token from the app's shared preferences
    private suspend fun checkToken(): String? {
        // get the name of the current account
        val name = appContext.getSharedPreferences(KEY_ACCOUNTS_DATA, Context.MODE_PRIVATE)
            .getString(KEY_CURR_ACCOUNT, null)

        // TODO if the token is expired, we need to refresh it
        return withContext(Dispatchers.IO) {
            name?.let { tokenStore.getTokenStore(it).access }
        }
    }
}