package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.data.models.PostListing;

public interface PostRepository {
  void getPosts(String subredditName);

  LiveData<PostListing> getPostListing(String subredditName);
  void retrieveNextPostListing(String listingAfter);
}
