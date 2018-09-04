package com.relic.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.relic.data.gateway.SubGateway;
import com.relic.data.models.SubredditModel;

import java.util.List;

public interface SubRepository {

  /**
   * @return whether all subscribed subs have been retrieved as livedata
   */
  LiveData<Boolean> getAllSubscribedSubsLoaded();

  /**
   * @return list of subscribed subs in the database as livedata
   */
  LiveData<List<SubredditModel>> getSubscribedSubs();

  /**
   * Fetches and stores more Subreddits from the Reddit API into the local database
   */
  void retrieveAllSubscribedSubs();

  /**
   * @param subName "friendly" subreddit name for the subreddit to retrieve
   * @return the subreddit model stored locally with the name that matches the subname param
   */
  LiveData<SubredditModel> getSingleSub(String subName);

  /**
   * Retrieves and parses the subreddit from network and
   * @param subName "friendly" subreddit name for subreddit to retrieve
   */
  void retrieveSingleSub(String subName);

  /**
   * Returns a list of subreddit names matching the search value
   * @param liveResults the livedata results list to be updated when results are parsed form the api
   * @param query query to find matching subreddits for
   */
  void searchSubreddits(MutableLiveData<List<String>> liveResults, String query);

  /**
   * @return subreddit gateway for more specific features relating to single subreddit
   */
  SubGateway getSubGateway();
}
