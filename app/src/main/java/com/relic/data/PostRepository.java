package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.data.models.PostListingModel;
import com.relic.data.models.PostModel;

import java.util.List;

public interface PostRepository {
  LiveData<List<PostModel>> getPostListing(String subredditName);

  void retrieveNextPostListing(String listingAfter);
}
