package com.relic.data

import androidx.lifecycle.LiveData

import com.relic.data.gateway.SubGateway
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface SubRepository {

    /**
     * @return list of subscribed subs in the database as livedata
     */
    fun getSubscribedSubs(): LiveData<List<SubredditModel>>

    /**
     * @return subreddit gateway for more specific features relating to single subreddit
     */
    fun getSubGateway(): SubGateway

    fun getPinnedsubs(): LiveData<List<SubredditModel>>

    suspend fun retrieveAllSubscribedSubs() : List<SubredditModel>

    suspend fun clearAndInsertSubs(subs : List<SubredditModel>)

    suspend fun insertSub(sub : SubredditModel)

    /**
     * @param subName "friendly" subreddit name for the subreddit to retrieve
     * @return the subreddit model stored locally with the name that matches the subname param
     */
    fun getSingleSub(subName: String): LiveData<SubredditModel>

    /**
     * Retrieves and parses the subreddit from network and
     * @param subName "friendly" subreddit name for subreddit to retrieve
     */
    suspend fun retrieveSingleSub(subName: String) : SubredditModel

    /**
     * Returns a list of subreddit names matching the search value
     * TODO include additional search settings
     * @param query query to find matching subreddits for
     */
    suspend fun searchSubreddits(query: String, displayNSFW : Boolean, exact : Boolean) :  List<SubPreviewModel>


    suspend fun pinSubreddit(subredditName: String, newPinnedStatus: Boolean)
}