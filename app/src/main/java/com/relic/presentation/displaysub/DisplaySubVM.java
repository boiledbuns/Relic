package com.relic.presentation.displaysub;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.relic.data.PostRepository;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.data.models.PostModel;
import com.relic.data.models.SubredditModel;

import java.util.List;


public class DisplaySubVM extends ViewModel implements DisplaySubContract.ViewModel, RetrieveNextListingCallback {
  private final String TAG = "DISPLAYSUB_VM";
  private SubredditModel currentSub;
  private PostRepository postRepo;

  private MediatorLiveData<List<PostModel>> postsMediator;

  public void init(SubredditModel subModel, PostRepository postRepo) {
    Log.d(TAG, subModel.getSubName());
    this.currentSub = subModel;
    this.postRepo = postRepo;

    // initialize the mediator
    postsMediator = new MediatorLiveData<>();
    postsMediator.setValue(null);

    postsMediator.addSource(postRepo.getPosts(currentSub.getSubName()), postsMediator::setValue);
  }


  /**
   * Exposes the livedata list of posts to the view
   * @return the lists of posts
   */
  public LiveData<List<PostModel>> getPosts() {
    return postsMediator;
  }


  @Override
  public String getSubName() {
    return currentSub.getSubName();
  }


  /**
   * Method that allows views aware of the VM to request the VM retrieve more posts
   */
  @Override
  public void retrieveMorePosts(boolean resetPosts) {
    if (resetPosts) {
      // we pass null for the value of next to tell repo that we're refreshing
      postRepo.retrieveMorePosts(currentSub.getSubName(), null);
    }
    else {
      // retrieve the "after" value for the next posting
      postRepo.getNextPostingVal(this, currentSub.getSubName());
    }
  }


  @Override
  public void onNextListing(String nextVal) {
    Log.d(TAG, "Retrieving next posts with " + nextVal);
    // retrieve the "after" value for the next posting
    postRepo.retrieveMorePosts(currentSub.getSubName(), nextVal);
  }

}
