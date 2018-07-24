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
  final String TAG = "DISPLAY_SUBS_VM";

  private SubRepository subRepo;
  private ListingRepository listingRepo;
  private Authenticator auth;

  private MediatorLiveData <List<SubredditModel>> obvSubsMediator;
  private MutableLiveData <List<String>> searchResults;

  private String prevQuery;


  public void init(SubRepository subRepository, ListingRepository ListingRepository, Authenticator auth) {
    subRepo = subRepository;
    listingRepo = ListingRepository;
    this.auth = auth;
    auth.refreshToken(this);

    // initialize the livedata with null value until we get db values
    searchResults = new MediatorLiveData<>();
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
  public LiveData<List<String>> getSearchResults() {
    return searchResults;
  }


  @Override
  public void retrieveSearchResults(String query) {
    Log.d(TAG, "Retrieving search results for " + query);

    // retrieves search results only if the query is non empty
    // also ignores first entry as query when the sea
    if (!query.isEmpty()) {
      if (prevQuery == null) {
        // TODO reinitialize
      }
      // replaces the current livedata with a new one based on new query string
      subRepo.searchSubreddits(searchResults, query);
      // sets the new query as the prev query
      prevQuery = query;
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
