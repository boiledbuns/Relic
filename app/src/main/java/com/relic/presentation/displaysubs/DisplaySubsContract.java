package com.relic.presentation.displaysubs;

public interface DisplaySubsContract {
  interface VM {

    void refreshSubs();

    /**
     * Makes request for matches based on the query
     * @param query search query to be sent to api
     */
    void retrieveSearchResults(String query);
  }
}
