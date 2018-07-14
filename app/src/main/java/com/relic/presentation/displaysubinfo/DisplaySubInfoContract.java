package com.relic.presentation.displaysubinfo;

import android.arch.lifecycle.LiveData;

import com.relic.data.SubRepository;

public interface DisplaySubInfoContract {
  interface ViewModel {
    void initialize(String subName, SubRepository subrepo);

    public LiveData<String> getDescription();

    public LiveData<Boolean> getSubscribed();
  }
}
