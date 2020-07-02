package com.relic.data.gateway

interface SubGateway {
    suspend fun retrieveAdditionalSubInfo(subredditName: String): String

    suspend fun retrieveSidebar(subredditName: String): String

    suspend fun getIsSubscribed(subredditName: String): Boolean

    suspend fun subscribe(isSubbed: Boolean, subreddit: String)

    suspend fun retrieveSubBanner(subreddit: String)
}
