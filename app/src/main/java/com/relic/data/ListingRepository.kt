package com.relic.data

interface ListingRepository {

    suspend fun insertAfter(source: PostSource, after : String?)

    suspend fun getAfter(source: PostSource): String?
}
