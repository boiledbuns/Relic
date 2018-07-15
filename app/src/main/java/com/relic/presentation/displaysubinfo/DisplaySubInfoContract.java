package com.relic.presentation.displaysubinfo;

import android.arch.lifecycle.LiveData;

import com.relic.data.SubRepository;
import com.relic.data.models.SubredditModel;

public interface DisplaySubInfoContract {
  interface ViewModel {
    void initialize(String subName, SubRepository subrepo);

    LiveData<SubredditModel> getSubreddit();
    void retrieveSubreddit();

    LiveData<String> getDescription();

    LiveData<Boolean> getSubscribed();

    LiveData<Boolean> subscribe();
    LiveData<Boolean> unsubscribe();

  }
}
