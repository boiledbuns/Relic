package com.relic.data

import android.arch.lifecycle.LiveData

import com.relic.data.gateway.SubGateway
import com.relic.domain.models.SubredditModel

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

    suspend fun clearAndInsertSubs(subs :List<SubredditModel>)

    /**
     * @param subName "friendly" subreddit name for the subreddit to retrieve
     * @return the subreddit model stored locally with the name that matches the subname param
     */
    fun getSingleSub(subName: String): LiveData<SubredditModel>

    /**
     * Retrieves and parses the subreddit from network and
     * @param subName "friendly" subreddit name for subreddit to retrieve
     */
    suspend fun retrieveSingleSub(subName: String)

    /**
     * Returns a list of subreddit names matching the search value
     * TODO include additional search settings
     * @param liveResults the livedata results list to be updated when results are parsed form the api
     * @param query query to find matching subreddits for
     */
    suspend fun searchSubreddits(query: String) :  List<String>


    suspend fun pinSubreddit(subredditName: String, newPinnedStatus: Boolean)
}

