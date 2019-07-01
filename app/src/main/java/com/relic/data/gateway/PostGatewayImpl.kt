package com.relic.data.gateway

import android.util.Log

import com.relic.persistence.ApplicationDB
import com.relic.data.DomainTransfer
import com.relic.persistence.entities.PostVisitRelation
import com.relic.data.repository.RepoConstants
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class PostGatewayImpl @Inject constructor(
    private val appDB : ApplicationDB,
    private val requestManager: NetworkRequestManager,
    private val moshi : Moshi
) : PostGateway {
    var TAG = "POST_GATEWAY"

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
        Timber.d("Setting {postFullname} to visited")
        withContext(Dispatchers.IO) {
            appDB.postVisitedDao.insertVisited(PostVisitRelation(postFullname))
        }
    }
}
