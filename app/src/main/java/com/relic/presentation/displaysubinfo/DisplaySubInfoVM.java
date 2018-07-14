package com.relic.presentation.displaysubinfo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.relic.data.SubRepository;
import com.relic.data.gateway.SubGateway;

public class DisplaySubInfoVM extends ViewModel implements DisplaySubInfoContract.ViewModel {
  private SubRepository subRepo;
  private SubGateway subGateway;

  private String subredditName;
  private LiveData<String> subDescription;
  private LiveData<Boolean> subscribed;

  @Override
  public void initialize(String subName, SubRepository subrepo) {
    this.subRepo = subRepo;
    this.subGateway = subrepo.getSubGateway();

    subredditName = subName;

    fetchValues();
  }


  private void fetchValues () {
    subDescription = subGateway.getAdditionalSubInfo(subredditName);
  }


  public LiveData<String> getDescription() {
    return subDescription;
  }

  public LiveData<Boolean> getSubscribed() {
    return subscribed;
  }

  public void subscribeToSubreddit() {

  }

  public void unsubscribeToSubreddit() {

  }
}
