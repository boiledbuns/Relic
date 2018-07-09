package com.relic.data;

import android.arch.lifecycle.LiveData;

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


  LiveData<SubredditModel> findSub(String name);
}
