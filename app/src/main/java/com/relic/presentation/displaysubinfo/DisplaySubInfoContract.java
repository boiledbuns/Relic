package com.relic.presentation.displaysubinfo;

import android.arch.lifecycle.LiveData;

import com.relic.data.SubRepository;

public interface DisplaySubInfoContract {
  interface ViewModel {
    void initialize(String subName, SubRepository subrepo);

    LiveData<String> getDescription();

    LiveData<Boolean> getSubscribed();

    LiveData<Boolean> subscribe();
    LiveData<Boolean> unsubscribe();

  }
}
