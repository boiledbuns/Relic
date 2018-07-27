package com.relic.presentation.displaysub;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import com.relic.data.PostRepository;
import com.relic.data.SubRepository;
import com.relic.domain.Subreddit;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.data.models.PostModel;
import com.relic.data.models.SubredditModel;

import java.util.List;


public class DisplaySubVM extends ViewModel implements DisplaySubContract.ViewModel, RetrieveNextListingCallback {
  private final String TAG = "DISPLAYSUB_VM";
  private boolean isInitialized = false;
  private SubredditModel currentSub;
  private SubRepository subRepo;
  private PostRepository postRepo;


  private MediatorLiveData<List<PostModel>> postListMediator;
  private LiveData<SubredditModel> subModel;

  public void init(SubredditModel subModel, SubRepository subRepo, PostRepository postRepo) {
    // ensure that the subreddit model is initialized only once
    if (!isInitialized) {
      Log.d(TAG, subModel.getSubName());
      this.currentSub = subModel;
      this.postRepo = postRepo;
      this.subRepo = subRepo;

      this.subModel = subRepo.getSingleSub(subModel.getSubName());

      // initialize the mediator for loading posts
      postListMediator = new MediatorLiveData<>();
      postListMediator.addSource(postRepo.getPosts(currentSub.getSubName()), new Observer<List<PostModel>>() {
        @Override
        public void onChanged(@Nullable List<PostModel> postModels) {
          if (postModels.isEmpty()) {
            Log.d(TAG, "empty posts");
            retrieveMorePosts(true);
          } else {
            Log.d(TAG, "not empty posts");
            postListMediator.setValue(postModels);
          }
        }

      });

      isInitialized = true;
    }
  }


  public LiveData<SubredditModel> getSubModel() {
    return subModel;
  }


  @Override
  public String getSubName() {
    return currentSub.getSubName();
  }


  /**
   * Exposes the livedata list of posts to the view
   * @return the lists of posts
   */
  public LiveData<List<PostModel>> getPosts() {
    return postListMediator;
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
