package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.data.Subreddit.SubredditDecorator;

import java.util.List;

public interface SubRepository {
  LiveData<List<SubredditDecorator>> getSubscribed();

}
