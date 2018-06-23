package com.relic.data;

import com.relic.presentation.callbacks.RetrieveNextListingCallback;

public interface ListingRepository {
  /**
   * Retrieves the "after" key and sends it back via the callback
   * @param key the key to the "after" value stored in the database
   * @param callback called after the "after" key is retrieved
   */
  void getKey(String key, RetrieveNextListingCallback callback);
}
