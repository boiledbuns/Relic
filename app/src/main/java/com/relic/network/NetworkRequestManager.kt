package com.relic.network

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.relic.network.request.RelicOAuthRequest
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Abstraction for all network requests
 */
class NetworkRequestManager (
    applicationContext: Context
) {

    // TODO move this to be injected
    private val volleyQueue: RequestQueue = VolleyAccessor.getInstance(applicationContext).requestQueue

    fun processRequest(relicRequest : RelicOAuthRequest) {
        volleyQueue.add(relicRequest)
    }

    suspend fun processRequest (
        method: Int,
        url: String,
        authToken: String
    ) : String = suspendCoroutine { cont ->

        val relicRequest = RelicOAuthRequest(
            method, url,
            Response.Listener { response: String ->
                cont.resumeWith(Result.success(response))
            },
            Response.ErrorListener { e: VolleyError ->
                cont.resumeWithException(e)
            },
            authToken
        )

        volleyQueue.add(relicRequest)
    }
}