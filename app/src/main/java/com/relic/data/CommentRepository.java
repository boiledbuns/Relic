package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.data.models.CommentModel;
import com.relic.data.models.PostModel;

import java.util.List;

public interface CommentRepository {

  /**
   * Exposes the comments as a livedata list
   * @param postFullname fullname of a post
   */
  LiveData<List<CommentModel>> getComments(String postFullname);

  /**
   * retrieves comments for a post from the network and stores them locally
   * @param subName display name of a subreddit
   * @param postFullname full name of the post
   * @param after "after" value for the comments listing
   */
  void retrieveComments(String subName, String postFullname, String after);

  /**
   * clears all locally stored comments
   * @param postFullname full name of the post to clear the comments for
   */
  void clearComments(String postFullname);

}
