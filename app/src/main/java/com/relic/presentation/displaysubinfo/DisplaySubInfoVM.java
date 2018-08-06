package com.relic.presentation.displaysubinfo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.relic.data.SubRepository;
import com.relic.data.gateway.SubGateway;
import com.relic.data.models.SubredditModel;

public class DisplaySubInfoVM extends ViewModel implements DisplaySubInfoContract.ViewModel {
  private SubRepository subRepo;
  private SubGateway subGateway;
  private LiveData<SubredditModel> subredditModel;

  private String subredditName;
  private LiveData<String> subDescription;
  private LiveData<Boolean> isSubbed;

  @Override
  public void initialize(String subName, SubRepository subRepo) {
    this.subRepo = subRepo;
    this.subGateway = subRepo.getSubGateway();

    subredditName = subName;

    fetchValues();
  }


  private void fetchValues () {
    subDescription = subGateway.getAdditionalSubInfo(subredditName);
    isSubbed = subGateway.getIsSubscribed(subredditName);

    subredditModel = subRepo.getSingleSub(subredditName);
  }

  public LiveData<SubredditModel> getSubreddit() {
    return subRepo.getSingleSub(subredditName);
  }

  @Override
  public void retrieveSubreddit() {
    subRepo.retrieveSingleSub("getnarwhal");
  }

  public LiveData<String> getDescription() {
    return subDescription;
  }

  public LiveData<Boolean> getSubscribed() {
    return isSubbed;
  }

  public LiveData<Boolean> subscribe() {
    //return subGateway.subscribe(subredditName);
    return new MutableLiveData<>();
  }

  public LiveData<Boolean> unsubscribe() {
    //return subGateway.unsubscribe(subredditName);
    return new MutableLiveData<>();

  }
}
