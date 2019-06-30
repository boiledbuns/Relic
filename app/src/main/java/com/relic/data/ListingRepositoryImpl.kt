package com.relic.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import com.relic.data.entities.ListingEntity
import dagger.Reusable

import kotlinx.coroutines.*

import javax.inject.Inject

@Reusable
class ListingRepositoryImpl @Inject constructor(
    private val appDB: ApplicationDB
) : ListingRepository {

    override suspend fun insertAfter(source: PostSource, after : String?) {
        if (after != null) {
            withContext(Dispatchers.IO) {
                val listing = ListingEntity(after = after, postSource = source.getSourceName())
                appDB.listingDAO.insertListing(listing)
            }
        }
    }

    override suspend fun getAfter(source: PostSource): String? {
        return withContext(Dispatchers.IO) { appDB.listingDAO.getAfterString(source.getSourceName()) }
    }
}
