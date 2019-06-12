package com.relic.data.gateway

import com.relic.api.response.Listing
import com.relic.data.PostRepository
import com.relic.domain.models.ListingItem

interface PostGateway {
    suspend fun voteOnPost(fullname: String, voteStatus: Int)

    suspend fun savePost(fullname: String, save: Boolean)

    suspend fun comment(fullname: String, comment: String)

    suspend fun gildPost(fullname: String, gild: Boolean)

    suspend fun reportPosts(fullname: String, report: Boolean)

    suspend fun visitPost(postFullname: String)

    /**
     * this method is used specifically to retrieve listings that can have both post and comments.
     * currently, this is only used when displaying users
     */
    suspend fun retrieveListingItems(
        source: PostRepository.PostSource,
        listingAfter: String? = null
    ) : Listing<out ListingItem>
}
