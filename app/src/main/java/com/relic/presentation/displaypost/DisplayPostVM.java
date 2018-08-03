package com.relic.presentation.displaypost;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import com.relic.data.CommentRepository;
import com.relic.data.ListingRepository;
import com.relic.data.PostRepository;
import com.relic.data.models.CommentModel;
import com.relic.data.models.PostModel;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;

import java.util.List;

public class DisplayPostVM extends ViewModel implements DisplayPostContract.ViewModel {
  private String TAG = "DISPLAYPOST_VM";

  private ListingRepository listingRepo;
  private PostRepository postRepo;
  private CommentRepository commentRepo;

  private MediatorLiveData<PostModel> currentPost;
  private MediatorLiveData<List<CommentModel>> commentList;
  private MediatorLiveData <String> commentListingKey;

  private String postFullname;
  private String subName;


  public void init(ListingRepository listingRepo, PostRepository postRepo, CommentRepository commentRepo, String subreddit, String fullname) {
    // initialize reference to repos for this VM
    this.listingRepo = listingRepo;
    this.postRepo = postRepo;
    this.commentRepo = commentRepo;

    subName = subreddit;
    postFullname = fullname;

    currentPost = new MediatorLiveData<>();
    commentList = new MediatorLiveData<>();
    commentListingKey = new MediatorLiveData<>();

    observeLivedata();
    retrieveMoreComments(false);
  }
  
  private void observeLivedata() {
    // retrieves the livedata post to be exposed to the view
    currentPost.addSource(postRepo.getPost(postFullname),
        (PostModel post) -> {
          if (post != null) {
            // TODO add additional actions to trigger when post loaded
            currentPost.setValue(post);
          }
        });

    // retrieve the comment list as livedata so that we can expose it to the view first
    commentList.addSource(commentRepo.getComments(postFullname),
        (List<CommentModel> comments) -> {
          if (comments != null) {
            commentList.setValue(comments);
          }
        });

    commentListingKey.addSource(listingRepo.getKey(),
        (String nextListingKey) -> {
          if (nextListingKey != null) {
            // retrieve the next listing using its key
            commentRepo.retrieveComments(subName, postFullname, nextListingKey);
          }
        });

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
      listingRepo.retrieveKey(postFullname);
    }
  }

}
