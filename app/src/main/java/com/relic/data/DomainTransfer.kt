package com.relic.data

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.NetworkError
import com.relic.data.deserializer.RelicParseException
import com.relic.data.repository.AuthException
import com.relic.data.repository.NetworkException
import com.relic.data.repository.RepoException

/**
 * Transforms library specific exceptions into more generic exceptions tied to repo interface
 * A bit overkill, but I'd prefer to have finer control over exception structure
 */
class DomainTransfer {

    companion object {
        private val TAG = "DOMAIN_TRANSFER"

        /**
         * transforms an exception we explicitly handle into a repo exception
         * st. internal implementation is not exposed to external layers
         *
         * if not handled, return null to indicate explicitly
         */
        fun handleException(message : String, e : Throwable) : RepoException? {
            Log.d(TAG, e.toString())
            return when (e) {
                is RelicParseException -> RepoException.ClientException(message, e)
                // exceptions from volley
                is AuthFailureError -> AuthException(message, e)
                is NetworkError -> NetworkException(message, e)
                // exception we catch but don't understand
                else -> null
            }
        }
    }
}
