package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.data.gateway.SubGateway;
import com.relic.data.models.SubredditModel;

import java.util.List;

public interface SubRepository {

  LiveData<List<SubredditModel>> getSubscribedSubs();

  /**
   * Fetches and stores more Subreddits from the Reddit API into the local database
   * @param after the "after" value to fetch the next list of subscribed subreddits (as the results
   *              are paged), refreshes if it is null
   */
  void retrieveMoreSubscribedSubs(String after);

  LiveData<SubredditModel> getSingleSub(String subName);
  void retrieveSingleSub(String subName);


  LiveData<List<SubredditModel>> findSubreddit(String name);

  /**
   * Returns a list of subreddit names matching the search value
   * @param searchValue
   * @return
   */
  LiveData<List<String>> searchSubreddits(String searchValue);

  SubGateway getSubGateway();
}
