package com.relic.network.request

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import java.util.*

/**
 * Volley String request with custom values configured for this application
 */

class RelicOAuthRequest @JvmOverloads constructor(
    method: Int,
    url: String,
    listener: Response.Listener<String>,
    errorListener: Response.ErrorListener,
    var authToken: String? = null,
    private val headers: MutableMap<String, String>? = null,
    private val data: MutableMap<String, String>? = null
) : StringRequest(method, url, listener, errorListener) {

    companion object {
        const val GET = Method.GET
        const val POST = Method.POST
    }

    private val userAgent = "android:com.relic.Relic (by /u/boiledbuns)"

    override fun getHeaders(): Map<String, String> {
        // generate the headers if not supplied string for oauth
        return headers ?: HashMap<String, String>().apply {
            val credentials = "bearer $authToken"
            put("Authorization", credentials)
            put("User-Agent", userAgent)
        }
    }

    override fun getParams(): MutableMap<String, String> {
        return data ?: HashMap()
    }
}
