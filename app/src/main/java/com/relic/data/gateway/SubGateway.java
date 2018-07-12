package com.relic.data.gateway;

import android.arch.lifecycle.LiveData;

public interface SubGateway {
  LiveData<String> getAdditionalSubInfo(String subredditName);

  void subscribe(String subreddit);
  void unsubscribe(String subreddit);
}
