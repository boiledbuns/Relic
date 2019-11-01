package com.relic.data.gateway

import com.relic.data.DomainTransfer
import com.relic.data.repository.RepoConstants
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.relic.persistence.ApplicationDB
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CommentGatewayImpl @Inject constructor(
  private val appDB : ApplicationDB,
  private val requestManager: NetworkRequestManager,
  private val moshi : Moshi
) : CommentGateway {

    override suspend fun voteOnComment(fullname: String, voteStatus: Int) {
        // generate the voting endpoint
        val ending = RepoConstants.ENDPOINT + "api/vote?id=" + fullname + "&dir=$voteStatus"
        try {
            requestManager.processRequest(RelicOAuthRequest.POST, ending)
            Timber.d("Success voting on comment : $fullname to $voteStatus")

            // update the local model appropriately
            withContext(Dispatchers.IO) {
                appDB.commentDAO.updateVote(fullname, voteStatus)
            }
        } catch (e : Exception) {
            throw DomainTransfer.handleException("vote on comment", e) ?: e
        }
    }

}