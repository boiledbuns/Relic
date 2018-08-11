package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.data.models.PostModel;

import java.util.List;

public interface PostRepository {
  int SORT_DEFAULT = 0;
  int SORT_BEST = 1;
  int SORT_CONTROVERSIAL = 2;
  int SORT_HOT = 3;
  int SORT_NEW = 4;
  int SORT_RISING = 5;
  int SORT_TOP = 6;

  int SCOPE_NONE = 0;
  int SCOPE_HOUR = 1;
  int SCOPE_DAY = 2;
  int SCOPE_WEEK = 3;
  int SCOPE_MONTH = 4;
  int SCOPE_YEAR = 5;
  int SCOPE_ALL = 6;

  /**
   * exposes livedata list of posts for a given subreddit
   * @param subredditName name of the subreddit to retrieve the posts for
   * @return list of posts from this subreddit as livedata (empty if none)
   */
  LiveData<List<PostModel>> getPosts(String subredditName);

  /**
   * retrieves more posts from the network and store them locally
   * @param subredditName valid subreddit name
   * @param postingAfter null ? refresh : "after" value for the next listing
   */
  void retrieveMorePosts(String subredditName, String postingAfter);

  /**
   *
   * @param callback
   * @param subName
   */
  void getNextPostingVal(RetrieveNextListingCallback callback, String subName);

  /**
   * exposes a single post model as livedata
   * @param postFullName a valid "full name" for a post
   * @return a model of the post wrapped in Livedata
   */
  LiveData<PostModel> getPost(String postFullName);

  /**
   * retrieves a single post from the network and stores it locally
   * @param subredditName name of subreddit that the post was made in
   * @param postFullName "full name" of the subreddit"
   */
  void retrievePost(String subredditName, String postFullName);

  /**
   * clears all current posts for this subreddit and retrieves new ones based on the sorting
   * method specified
   * @param subredditName
   * @param sortByCode
   */
  void retrieveSortedPosts(String subredditName, int sortByCode, int sortScopeCode);


  void retrieveSortedPosts(String subredditName, int sortByCode);

  /**
   * //TODO tentative -> should expose or not
   * need to decide whether the Viewmodel should handle this or not
   * @param subredditName
   */
  void clearAllSubPosts(String subredditName);
}
