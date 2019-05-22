package com.relic.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.relic.data.dao.SubredditDao
import com.relic.data.deserializer.Contract
import com.relic.data.deserializer.SubDeserializerImpl
import com.relic.data.entities.SubredditEntity
import com.relic.data.gateway.SubGateway
import com.relic.data.gateway.SubGatewayImpl
import com.relic.data.models.SubredditModel
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

import java.util.ArrayList

class SubRepositoryImpl(private val context: Context, private val requestManager: NetworkRequestManager) : SubRepository {
    private val TAG = "SUB_REPO"

    private val ENDPOINT = "https://oauth.reddit.com/"

    private val subDao: SubredditDao = ApplicationDB.getDatabase(context).subredditDao
    private val parser: JSONParser = JSONParser()
    private val gson: Gson = GsonBuilder().create()

    // TODO convert to injection
    private val subDeserializer: Contract.SubDeserializer = SubDeserializerImpl(context)

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
        val response = requestManager.processRequest(
            method = RelicOAuthRequest.GET,
            url = ENDPOINT + "r/" + subName + "/about"
        )
        Log.d(TAG, response)

        try {
            // parse the response and add it to an arraylist to be inserted in the db
            val subredditObject = (parser.parse(response) as JSONObject)["data"] as JSONObject?
            val subreddit = gson.fromJson(subredditObject!!.toJSONString(), SubredditEntity::class.java)

            // create a new task to insert the subreddits on parse success
            withContext(Dispatchers.IO) {
                subDao.insert(subreddit)
            }
        } catch (e: ParseException) {
            Log.d(TAG, "There was an error retrieving the response from the server " + e.message)
        }
    }

    override suspend fun searchSubreddits(query: String) : List<String>{
        val response = requestManager.processRequest(
            method = RelicOAuthRequest.POST,
            url = ENDPOINT + "api/search_subreddits?query=" + query
        )

        return subDeserializer.parseSearchedSubs(response)
    }

    override fun getSubGateway(): SubGateway {
        return SubGatewayImpl(context, requestManager)
    }

    override suspend fun pinSubreddit(subredditName: String, newPinnedStatus: Boolean) {
        subDao.updatePinnedStatus(subredditName, newPinnedStatus)
    }

    override fun getPinnedsubs(): LiveData<List<SubredditModel>> {
        return subDao.allPinnedSubs
    }


    /**
     * Handles retrieval of subreddits from the network
     * @param after: null if retrieving from scratch, only include if retrieving *MORE* subs
     */
    private suspend fun retrieveMoreSubscribedSubs(after: String?, callback: SubsLoadedCallback?) {
        var ending = "subreddits/mine/subscriber?limit=30"
        after?.let { ending += "&after=$after" }

        val response = requestManager.processRequest(
            method = RelicOAuthRequest.GET,
            url = ENDPOINT + ending
        )

        val newAfter = ((parser.parse(response) as JSONObject)["data"] as JSONObject)["after"] as String?
        val subs = subDeserializer.parseSubreddits(response)

        withContext(Dispatchers.IO) { subDao.insertAll(subs) }

        if (newAfter != null) {
            Log.d(TAG, "after = " + newAfter!!)
            // checks the after value of the listing for the current subs
            // retrieve more subs without refreshing if the string is null
            retrieveMoreSubscribedSubs(newAfter, callback)
        }
        else {
            // if no after value, invoke callback method
            callback?.callback()
        }
    }
}
