package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.data.models.PostModel;

import java.util.List;

public interface PostRepository {
  int SORT_BEST = 1;
  int SORT_CONTROVERSIAL = 2;
  int SORT_HOT = 3;
  int SORT_NEW = 4;
  int SORT_RANDOM = 5;
  int SORT_RISING = 6;
  int SORT_TOP = 7;

  LiveData<List<PostModel>> getPosts(String subredditName);

  void retrieveMorePosts(String subredditName, String postingAfter);

  void getNextPostingVal(RetrieveNextListingCallback callback, String subName);

  LiveData<PostModel> getPost(String postFullName);

  void retrievePost(String subredditName, String postFullName);

  void retrieveSortedPosts(String subredditName, int sortByCode);

}
