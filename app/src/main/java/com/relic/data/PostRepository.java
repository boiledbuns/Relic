package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.data.models.PostListingModel;

public interface PostRepository {
  LiveData<PostListingModel> getPostListing(String subredditName);

  void retrieveNextPostListing(String listingAfter);
}
