package com.relic.presentation.displaypost;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.relic.data.CommentRepository;
import com.relic.data.ListingRepository;
import com.relic.data.PostRepository;
import com.relic.data.models.CommentModel;
import com.relic.data.models.PostModel;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;

import java.util.List;

public class DisplayPostVM extends ViewModel implements DisplayPostContract.ViewModel, RetrieveNextListingCallback {
  private String TAG = "DISPLAYPOST_VM";

  private ListingRepository listingRepo;
  private PostRepository postRepo;
  private CommentRepository commentRepo;

  private LiveData<PostModel> currentPost;
  private LiveData<List<CommentModel>> commentList;

  private String postFullname;
  private String subName;


  public void init(ListingRepository listingRepo, PostRepository postRepo, CommentRepository commentRepo, String subreddit, String fullname) {
    // initialize reference to repos for this VM
    this.listingRepo = listingRepo;
    this.postRepo = postRepo;
    this.commentRepo = commentRepo;

    // retrieves the livedata post to be exposed to the view
    currentPost = postRepo.getPost(fullname);
    subName = subreddit;
    postFullname = fullname;

    // retrieve the comment list as livedata so that we can expose it to the view first
    commentList = commentRepo.getComments(fullname);
    retrieveMoreComments(false);
  }


  public void refresh() {

  }


  /**
   * Exposes the post to the view
   * @return post as livedata
   */
  public LiveData<PostModel> getPost() {
    return currentPost;
  }


  /**
   * Exposes the list of comments to the view
   * @return comment list as livedata
   */
  public LiveData<List<CommentModel>> getCommentList() {
    return commentList;
  }


  @Override
  public void retrieveMoreComments(boolean refresh) {
    if (refresh) {
      // TODO: delete the first set of comments
      // retrieve the first set of comments
      commentRepo.retrieveComments(subName, postFullname, null);
    }
    else {
      // retrieve the next listing for the comments on this post
      listingRepo.getKey(postFullname, this);
    }
  }


  @Override
  public void onNextListing(String nextVal) {
    Log.d(TAG, "Next Value " + nextVal);
    // retrieve the first set of comments
    commentRepo.retrieveComments(subName, postFullname, nextVal);
  }
}
