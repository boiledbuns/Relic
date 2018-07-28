package com.relic.data.gateway;

import android.arch.lifecycle.LiveData;

public interface SubGateway {
  LiveData<String> getAdditionalSubInfo(String subredditName);
  LiveData<Boolean> getIsSubscribed(String subredditName);

  LiveData<Boolean> subscribe(String subreddit);
  LiveData<Boolean> unsubscribe(String subreddit);

  void retrieveSubBanner(String subreddit);
}
