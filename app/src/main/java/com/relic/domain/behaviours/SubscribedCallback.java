package com.relic.domain.behaviours;

import com.relic.domain.Subreddit;

import java.util.List;

public interface SubscribedCallback {
  void recieveSubs(List<Subreddit> subList);

}
