package com.relic.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask

import kotlinx.coroutines.*

import javax.inject.Inject

class ListingRepositoryImpl @Inject constructor(
    private val appDB: ApplicationDB
) : ListingRepository {
    private val TAG = "LISTING_REPO"

    // TODO fix this. Not going to do it in this commit, but this is a terrible way to do it
    // initialize the listing key
    private val listingKey = MutableLiveData <String> ()

    override val key: LiveData<String>
        get() = listingKey

    override fun retrieveKey(key: String) {
        RetrieveListingAfterTask().execute(appDB, listingKey, key)
    }

    internal class RetrieveListingAfterTask : AsyncTask<Any, Unit, Unit>() {
        override fun doInBackground(vararg args: Any) {
            val appDB: ApplicationDB = args[0] as ApplicationDB
            val listingKey = args[1] as MutableLiveData<String>
            val key = args[2] as String

            // get the "after" value for the string value
            listingKey.postValue(appDB.listingDAO.getNext(key))
        }
    }

    override fun getAfter(fullName: String): LiveData<String?> {
        return appDB.listingDAO.getAfter(fullName)
    }

    override suspend fun getAfterString(fullName: String): String? {
        return withContext(Dispatchers.IO) { appDB.listingDAO.getAfterString(fullName) }
    }
}
