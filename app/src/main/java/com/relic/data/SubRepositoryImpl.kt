package com.relic.data

import android.arch.lifecycle.LiveData
import android.util.Log
import com.relic.api.response.Listing
import com.relic.data.deserializer.Contract
import com.relic.data.gateway.SubGateway
import com.relic.data.gateway.SubGatewayImpl
import com.relic.data.repository.RepoConstants.ENDPOINT
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.relic.persistence.ApplicationDB
import com.relic.persistence.dao.SubredditDao
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Reusable
class SubRepositoryImpl @Inject constructor(
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

    override suspend fun retrieveAllSubscribedSubs() : List<SubredditModel> {
        // since refreshing, set all subs loaded to reflect that not all subs are loaded
        // delete all locally stored subs
        return withContext(Dispatchers.IO) {
            val subs = ArrayList<SubredditModel>()
            var after : String? = null

            do {
                retrieveMoreSubscribedSubs(after).data.let { data ->
                    data.children?.let { subs.addAll(it) }
                    after = data.after
                }
            } while (after != null)

            subs.apply {
                sortBy { it.subName }
            }
        }
    }

    override suspend fun clearAndInsertSubs(subs: List<SubredditModel>) {
        withContext(Dispatchers.IO) {
            subDao.deleteAllSubscribed()
            subDao.insertAll(subs)
        }
    }

    override suspend fun insertSub(sub: SubredditModel) {
        withContext(Dispatchers.IO) {
            subDao.insert(sub)
        }
    }

    override fun getSingleSub(subName: String): LiveData<SubredditModel> {
        return subDao.getSub(subName)
    }

    override suspend fun retrieveSingleSub(subName: String) : SubredditModel{
        val url = "${ENDPOINT}r/{subName}/about"

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = url
            )

            return subDeserializer.parseSubredditResponse(response)
        } catch (e: Exception) {
            throw DomainTransfer.handleException("retrieve single sub", e) ?: e
        }
    }

    override suspend fun searchSubreddits(query: String) : List<SubPreviewModel>{
        val url = "${ENDPOINT}api/search_subreddits?query=$query"

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.POST,
                url = url
            )

            return subDeserializer.parseSearchSubsResponse(response)
        } catch (e: Exception) {
            throw DomainTransfer.handleException("search subs", e) ?: e
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
    private suspend fun retrieveMoreSubscribedSubs(after: String?) : Listing<SubredditModel> {
        var ending = "subreddits/mine/subscriber?limit=30"
        after?.let { ending += "&after=$after" }

        try {
            val response = requestManager.processRequest(
                method = RelicOAuthRequest.GET,
                url = ENDPOINT + ending
            )
            return subDeserializer.parseSubredditsResponse(response)
        } catch (e : Exception){
            throw DomainTransfer.handleException("retrieve more subsribed subs", e) ?: e
        }
    }
}
