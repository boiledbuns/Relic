package com.relic.data;

import com.relic.domain.Subreddit;
import com.relic.domain.behaviours.SubscribedCallback;
import com.relic.presentation.displaysubs.DisplaySubsContract;

import java.util.List;

public interface SubRepository {
  void getSubscribed(SubscribedCallback subCallback);

}
