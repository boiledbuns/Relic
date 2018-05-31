package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.data.models.SubredditModel;

import java.util.List;

public interface SubRepository {
  void getSubscribed();

  LiveData<List<SubredditModel>> getSubscribedList();

}
