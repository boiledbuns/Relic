package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LiveData;

import com.relic.data.Authenticator;
import com.relic.data.ListingRepository;
import com.relic.data.SubRepository;
import com.relic.data.models.SubredditModel;
import com.relic.domain.Subreddit;
import com.relic.presentation.subinfodialog.SubInfoDialogContract;

import java.util.List;

public interface DisplaySubsContract {
  interface VM {
    LiveData<Boolean> getAllSubscribedSubsLoaded();

    LiveData<List<SubredditModel>> getSubscribedList();

    void retrieveMoreSubs(boolean resetPosts);

    /**
     * Makes request for matches based on the query
     * @param query search query to be sent to api
     */
    void retrieveSearchResults(String query);
  }
}
