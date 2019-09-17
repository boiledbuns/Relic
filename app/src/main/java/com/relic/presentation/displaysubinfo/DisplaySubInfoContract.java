package com.relic.presentation.displaysubinfo;

import androidx.lifecycle.LiveData;

import com.relic.data.SubRepository;
import com.relic.domain.models.SubredditModel;

public interface DisplaySubInfoContract {
  interface ViewModel {
    void initialize(String subName, SubRepository subrepo);

    LiveData<SubredditModel> getSubreddit();
    void retrieveSubreddit();

    LiveData<Boolean> subscribe();
    LiveData<Boolean> unsubscribe();

  }
}
