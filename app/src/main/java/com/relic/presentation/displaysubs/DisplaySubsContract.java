package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LiveData;

import com.relic.data.Authenticator;
import com.relic.data.ListingRepository;
import com.relic.data.SubRepository;
import com.relic.data.models.SubredditModel;
import com.relic.domain.Subreddit;

import java.util.List;

public interface DisplaySubsContract {
  interface VM {
    void init(SubRepository subRepo, ListingRepository listingRepo, Authenticator auth);

    LiveData<List<SubredditModel>> getSubscribedList();

    void retrieveMoreSubs(boolean resetPosts);


    /**
     * Exposes the search results livedata to the view
     * @return livedata wrapping search results
     */
    LiveData<List<String>> getSearchResults();


    /**
     * Makes request for matches based on the query
     * @param query search query to be sent to api
     */
    void retrieveSearchResults(String query);
  }

}
