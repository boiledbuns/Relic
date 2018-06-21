package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;

import com.relic.data.Authenticator;
import com.relic.data.SubRepository;
import com.relic.presentation.callbacks.AuthenticationCallback;
import com.relic.data.models.SubredditModel;

import java.util.List;

public class DisplaySubsVM extends ViewModel implements DisplaySubsContract.VM, AuthenticationCallback {
  private SubRepository subRepo;
  private MediatorLiveData <List<SubredditModel>> obvSubsMediator;
  private Authenticator auth;

  final String TAG = "DISPLAY_SUBS_VM";

  public void init(SubRepository subRepository, Authenticator auth) {
    this.subRepo = subRepository;
    this.auth = auth;

    // initialize the mediator with null value until we get db values
    obvSubsMediator = new MediatorLiveData<>();
    obvSubsMediator.setValue(null);

    // add the live data from the repo as a source
    obvSubsMediator.addSource(subRepo.getSubscribedSubs(), obvSubsMediator::setValue);
  }


  /**
   * Exposes subscribed subs to the ui for binding
   * @return the livedata list of subscribed subs
   */
  @Override
  public LiveData<List<SubredditModel>> getSubscribedList() {
    return obvSubsMediator;
  }


  /**
   * Retrieves more posts either from the start or leading off the current "after" page
   * @param resetPosts whether the post list should be restarted
   */
  @Override
  public void retrieveMoreSubs(boolean resetPosts) {
    if (resetPosts) {
      // refresh token before performing any requests
      auth.refreshToken(this);
      subRepo.retrieveMoreSubscribedSubs(null);
    }
  }


  @Override
  public void onAuthenticated() {
    // retrieve posts from beginning because user has just been authenticated
    subRepo.retrieveMoreSubscribedSubs(null);
  }
}
