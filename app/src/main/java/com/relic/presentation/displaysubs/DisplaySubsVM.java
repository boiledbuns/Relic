package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import com.relic.data.Authenticator;
import com.relic.data.ListingRepository;
import com.relic.data.SubRepository;
import com.relic.presentation.callbacks.AuthenticationCallback;
import com.relic.data.models.SubredditModel;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;

import java.util.List;

public class DisplaySubsVM extends ViewModel implements DisplaySubsContract.VM, AuthenticationCallback, RetrieveNextListingCallback{
  private final String TAG = "DISPLAY_SUBS_VM";
  private boolean initialized;

  private SubRepository subRepo;
  private ListingRepository listingRepo;
  private Authenticator authenticator ;

  private MediatorLiveData <List<SubredditModel>> obvSubsMediator;
  private MutableLiveData <List<String>> searchResults;


  public void init(SubRepository subRepository, ListingRepository ListingRepository, Authenticator auth) {
    // initialize the viewmodel only if it hasn't already been initialized
    if (!initialized) {
      Log.d(TAG, "subreddit initialized");

      subRepo = subRepository;
      listingRepo = ListingRepository;
      authenticator = auth;
      auth.refreshToken(this);

      // initialize the livedata with null value until we get db values
      searchResults = new MediatorLiveData<>();
      searchResults.setValue(null);

      // initialize the subreddit mediator with subscribed subs data
      obvSubsMediator = new MediatorLiveData<>();
      obvSubsMediator.addSource(subRepo.getSubscribedSubs(), obvSubsMediator::setValue);

      initialized = true;
    }
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
      authenticator.refreshToken(this);
      subRepo.retrieveMoreSubscribedSubs(null);
    }
  }


  @Override
  public LiveData<List<String>> getSearchResults() {
    return searchResults;
  }


  @Override
  public void retrieveSearchResults(String query) {
    Log.d(TAG, "Retrieving search results for " + query);

    // retrieves search results only if the query is non empty
    // also ignores first query entry (always empty)
    if (!query.isEmpty()) {
      // replaces the current livedata with a new one based on new query string
      subRepo.searchSubreddits(searchResults, query);
    }
  }


  @Override
  public void onAuthenticated() {
    listingRepo.getKey("SUB_REPO", this);
  }


  @Override
  public void onNextListing(String nextVal) {
    // retrieve posts only if the "after" value is empty
    if (nextVal == null) {
      subRepo.retrieveMoreSubscribedSubs(null);
    }
  }
}
