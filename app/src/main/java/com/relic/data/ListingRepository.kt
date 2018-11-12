package com.relic.data

import android.arch.lifecycle.LiveData

import com.relic.presentation.callbacks.RetrieveNextListingCallback

interface ListingRepository {

    val key: LiveData<String>

    /**
     * Retrieves the "after" key and sends it back via the callback
     * @param key the key to the "after" value stored in the database
     */
    fun retrieveKey(key: String)

    fun getAfter(fullName: String): LiveData<String?>
}
