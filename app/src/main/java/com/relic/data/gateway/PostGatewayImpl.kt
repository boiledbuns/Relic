package com.relic.data.gateway

import android.content.Context
import android.util.Log

import com.relic.data.ApplicationDB
import com.relic.data.DomainTransfer
import com.relic.data.repository.RepoConstants
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostGatewayImpl(context: Context, private val requestManager: NetworkRequestManager) : PostGateway {
    var TAG = "POST_GATEWAY"

    private val appDb: ApplicationDB = ApplicationDB.getDatabase(context)

    override suspend fun voteOnPost(fullname: String, voteStatus: Int) {
        // generate the voting endpoint
        var ending = RepoConstants.ENDPOINT + "api/vote?id=" + fullname + "&dir=$voteStatus"
        try {
            requestManager.processRequest(RelicOAuthRequest.POST, ending)
            Log.d(TAG, "Success voting on post : $fullname to $voteStatus")

            // update the local model appropriately
            withContext(Dispatchers.IO) {
                appDb.postDao.updateVote(fullname, voteStatus)
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
                appDb.postDao.updateSave(fullname, save)
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
            appDb.postDao.updateVisited(postFullname)
        }
    }
}
