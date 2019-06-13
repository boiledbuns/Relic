package com.relic.data.gateway

import android.util.Log
import com.relic.api.response.Listing

import com.relic.data.ApplicationDB
import com.relic.data.DomainTransfer
import com.relic.data.PostRepository
import com.relic.data.repository.RepoConstants
import com.relic.data.repository.RepoConstants.ENDPOINT
import com.relic.data.repository.RepoException
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostGatewayImpl @Inject constructor(
    private val appDB : ApplicationDB,
    private val requestManager: NetworkRequestManager,
    private val moshi : Moshi
) : PostGateway {
    var TAG = "POST_GATEWAY"

    private val type = Types.newParameterizedType(Listing::class.java, ListingItem::class.java)
    private val listingAdapter = moshi.adapter<Listing<ListingItem>>(type)

    override suspend fun voteOnPost(fullname: String, voteStatus: Int) {
        // generate the voting endpoint
        val ending = RepoConstants.ENDPOINT + "api/vote?id=" + fullname + "&dir=$voteStatus"
        try {
            requestManager.processRequest(RelicOAuthRequest.POST, ending)
            Log.d(TAG, "Success voting on post : $fullname to $voteStatus")

            // update the local model appropriately
            withContext(Dispatchers.IO) {
                appDB.postDao.updateVote(fullname, voteStatus)
            }
        } catch (e : Exception) {
            throw DomainTransfer.handleException("vote on post", e) ?: e
        }
    }

    override suspend fun savePost(fullname: String, save: Boolean) {
        // generate the voting endpoint
        val saveString = if (save) "save" else "unsave"
        val ending = RepoConstants.ENDPOINT + "api/" + saveString + "?id=" + fullname
        try {
            requestManager.processRequest(RelicOAuthRequest.POST, ending)
            Log.d(TAG, "Success post saved status for $fullname to $save")

            // update the local model appropriately
            withContext(Dispatchers.IO) {
                appDB.postDao.updateSave(fullname, save)
            }
        } catch (e : Exception) {
            throw DomainTransfer.handleException("save post", e) ?: e
        }
    }


    override suspend fun comment(fullname: String, comment: String) {
        // TODO
    }

    override suspend fun gildPost(fullname: String, gild: Boolean) {
        // TODO
    }

    override suspend fun reportPosts(fullname: String, report: Boolean) {
        // TODO
    }

    override suspend fun visitPost(postFullname: String){
        Log.d(TAG, "Setting " + postFullname + "to visited")
        withContext(Dispatchers.IO) {
            appDB.postDao.updateVisited(postFullname)
        }
    }

    override suspend fun retrieveListingItems(
        source: PostRepository.PostSource,
        listingAfter: String?
    ) : Listing<out ListingItem> {
        // change the api endpoint to access the next post listing
        val ending = when (source) {
            is PostRepository.PostSource.Subreddit -> "r/${source.subredditName}"
            is PostRepository.PostSource.User -> "user/${source.username}/${source.retrievalOption.name.toLowerCase()}"
            else -> ""
        }

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = "$ENDPOINT$ending?after=$listingAfter"
            )
            Log.d(TAG, "listing items $response")

            return listingAdapter.fromJson(response) ?: throw RepoException.ClientException("retrieve more posts", null)
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve more posts", e) ?: e
        }
    }

    
}
