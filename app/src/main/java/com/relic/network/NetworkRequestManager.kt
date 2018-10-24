package com.relic.network

import android.content.Context
import com.android.volley.RequestQueue
import com.relic.network.request.RelicOAuthRequest

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

}