package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;

import com.relic.data.Authenticator;
import com.relic.data.SubRepository;
import com.relic.domain.Subreddit;

import java.util.List;

public class DisplaySubsVM extends ViewModel implements DisplaySubsContract.VM {
  private SubRepository subRepo;
  private MediatorLiveData <List<? extends Subreddit>> obvSubsMediator;

  final String TAG = "DISPLAY_SUBS_VM";

  public void init(SubRepository subRepository, Authenticator auth) {
    // refresh token before performing any requests
    auth.refreshToken();

    this.subRepo = subRepository;
    subRepository.getSubscribed();

    // initialize the mediator with null value until we get db values
    obvSubsMediator = new MediatorLiveData<>();
    obvSubsMediator.setValue(null);

    // add the live data from the repo as a source
    obvSubsMediator.addSource(subRepo.getSubscribedList(), obvSubsMediator::setValue);
  }

  /**
   * Exposes subscribed subs to the ui for binding
   * @return the livedata list of subscribed subs
   */
  @Override
  public LiveData<List<? extends Subreddit>> getSubscribed() {
    return obvSubsMediator;
  }


}
