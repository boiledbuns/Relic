package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.presentation.callbacks.RetrieveNextListingCallback;

public interface ListingRepository {

  LiveData<String> getKey();

  /**
   * Retrieves the "after" key and sends it back via the callback
   * @param key the key to the "after" value stored in the database
   */
  void retrieveKey(String key);

  LiveData<String> getAfter(String fullName);

}
