package com.relic.network.request

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest

import java.util.HashMap

/**
 * Volley String request with custom values configured for this application
 */
class RelicOAuthRequest(
        method: Int,
        url: String,
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener,
        private val authToken: String
) : StringRequest(method, url, listener, errorListener) {

    companion object {
        const val GET = Request.Method.GET
        const val POST = Request.Method.POST
    }

    private val userAgent = "android:com.relic.Relic (by /u/boiledbuns)"

    override fun getHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()

        // generate the credential string for oauth
        val credentials = "bearer $authToken"
        headers["Authorization"] = credentials
        headers["User-Agent"] = userAgent

        return headers
    }
}