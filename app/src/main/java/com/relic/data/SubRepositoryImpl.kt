package com.relic.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log

import com.relic.data.dao.SubredditDao
import com.relic.data.deserializer.Contract
import com.relic.data.gateway.SubGateway
import com.relic.data.gateway.SubGatewayImpl
import com.relic.domain.models.SubredditModel
import com.relic.data.repository.RepoConstants.ENDPOINT
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class SubRepositoryImpl @Inject constructor(
    private val context: Context,
    private val requestManager: NetworkRequestManager,
    private val appDb : ApplicationDB,
    private val subDeserializer: Contract.SubDeserializer
) : SubRepository {
    private val TAG = "SUB_REPO"

    private val subDao: SubredditDao = appDb.subredditDao

    // region interface methods

    override fun getSubscribedSubs(): LiveData<List<SubredditModel>> {
        return subDao.allSubscribed
    }

    override suspend fun retrieveAllSubscribedSubs(callback: SubsLoadedCallback) {
        // since refreshing, set all subs loaded to reflect that not all subs are loaded
        // delete all locally stored subs
        withContext(Dispatchers.IO) {
            subDao.deleteAllSubscribed()
            retrieveMoreSubscribedSubs(null, callback)
        }
    }

    override fun getSingleSub(subName: String): LiveData<SubredditModel> {
        return subDao.getSub(subName)
    }

    override suspend fun retrieveSingleSub(subName: String) {
        val url = "{ENDPOINT}r/{subName}/about"

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = url
            )
            Log.d(TAG, response)

            val subreddit = subDeserializer.parseSubredditResponse(response)

            // create a new task to insert the subreddits on parse success
            withContext(Dispatchers.IO) {
                subDao.insert(subreddit)
            }
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve single sub", e) ?: e
        }
    }

    override suspend fun searchSubreddits(query: String) : List<String>{
        val url = "{ENDPOINT}api/search_subreddits?query={query}"

        return try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.POST,
                url = url
            )

            subDeserializer.parseSearchSubsResponse(response)
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve single sub", e) ?: e
        }
    }

    override fun getSubGateway(): SubGateway {
        return SubGatewayImpl(appDb, requestManager)
    }

    override suspend fun pinSubreddit(subredditName: String, newPinnedStatus: Boolean) {
        withContext(Dispatchers.IO) { subDao.updatePinnedStatus(subredditName, newPinnedStatus) }
    }

    override fun getPinnedsubs(): LiveData<List<SubredditModel>> {
        return subDao.allPinnedSubs
    }

    // endregion interface methods


    /**
     * Handles retrieval of subreddits from the network
     * @param after: null if retrieving from scratch, only include if retrieving *MORE* subs
     */
    private suspend fun retrieveMoreSubscribedSubs(after: String?, callback: SubsLoadedCallback?) {
        var ending = "subreddits/mine/subscriber?limit=30"
        after?.let { ending += "&after=$after" }

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = ENDPOINT + ending
            )

            val subsData = subDeserializer.parseSubredditsResponse(response)

            withContext(Dispatchers.IO) { subDao.insertAll(subsData.subsList) }

            if (subsData.after != null) {
                Log.d(TAG, "after = " + subsData.after)
                // checks the after value of the listing for the current subs
                // retrieve more subs without refreshing if the string is null
                retrieveMoreSubscribedSubs(subsData.after, callback)
            } else {
                // if no after value, invoke callback method
                callback?.callback()
            }
        } catch (e : Exception){
            throw DomainTransfer.handleException("retrieve more subsribed subs", e) ?: e
        }
    }
}
