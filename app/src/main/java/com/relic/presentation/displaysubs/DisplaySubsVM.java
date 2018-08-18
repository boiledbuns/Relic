package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
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
  private boolean refreshing;

  private SubRepository subRepo;
  private ListingRepository listingRepo;
  private Authenticator authenticator ;

  private MediatorLiveData <List<SubredditModel>> subscribedSubsMediator;
  private MutableLiveData <List<String>> searchResults;
  private MediatorLiveData<Boolean> onAllSubsLoaded;


  public void init(SubRepository subRepository, ListingRepository ListingRepository, Authenticator auth) {
    // initialize the viewmodel only if it hasn't already been initialized
    if (!initialized) {
      Log.d(TAG, "subreddit initialized");
      // initialize references to repos objects
      subRepo = subRepository;
      listingRepo = ListingRepository;
      authenticator = auth;

      // initialize the mediator live data and associated livedata
      searchResults = new MediatorLiveData<>();
      searchResults.setValue(null);

      // initialize the subreddit mediator with subscribed subs data
      subscribedSubsMediator = new MediatorLiveData<>();
      subscribedSubsMediator.setValue(null);

      onAllSubsLoaded = new MediatorLiveData<>();
      onAllSubsLoaded.setValue(null);

      initializeObservers();
      initialized = true;
    }
  }


  /**
   * Initialize observers for livedata
   */
  private void initializeObservers () {
    //subscribedSubsMediator.addSource(subRepo.getSubscribedSubs(), subscribedSubsMediator::setValue);
    subscribedSubsMediator.addSource(subRepo.getSubscribedSubs(), (List<SubredditModel> subscribedSubs) -> {
      Log.d(TAG, " subs loaded " + subscribedSubs);
      if (subscribedSubs.isEmpty()) {
        // refresh the token even if the vm has already been initialized
        subRepo.retrieveMoreSubscribedSubs(null);
        //authenticator.refreshToken(this);
      } else {
        subscribedSubsMediator.setValue(subscribedSubs);
      }
    });

//    onAllSubsLoaded.addSource(subRepo.getAllSubscribedSubsLoaded(), (Boolean allSubsLoaded) -> {
//      if
//    });
//    // Observe changes to keys to request new data
//    subscribedSubsMediator.addSource(listingRepo.getKey(), (@Nullable String nextVal) -> {
//      // retrieve posts only if the "after" value is empty
//      subRepo.retrieveMoreSubscribedSubs(nextVal);
//    });
  }


  @Override
  public LiveData<Boolean> getAllSubscribedSubsLoaded() {
    return subRepo.getAllSubscribedSubsLoaded();
  }

  /**
   * Exposes subscribed subs to the ui for binding
   * @return the livedata list of subscribed subs
   */
  @Override
  public LiveData<List<SubredditModel>> getSubscribedList() {
    return subscribedSubsMediator;
  }


  /**
   * Retrieves more posts either from the start or leading off the current "after" page
   * @param resetPosts whether the post list should be restarted
   */
  @Override
  public void retrieveMoreSubs(boolean resetPosts) {
    if (resetPosts) {
      refreshing = true;
      // refresh token before performing any requests
      authenticator.refreshToken(this);
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
    //listingRepo.retrieveKey("SUB_REPO");
    Log.d(TAG, "On authenticated called");
    subRepo.retrieveMoreSubscribedSubs(null);
  }


  @Override
  public void onNextListing(String nextVal) {
    // retrieve posts only if the "after" value is empty
    if (nextVal == null) {
      subRepo.retrieveMoreSubscribedSubs(null);
    }
  }
}
