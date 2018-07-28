package com.relic.presentation.displaysub;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import com.relic.data.PostRepository;
import com.relic.data.SubRepository;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.data.models.PostModel;
import com.relic.data.models.SubredditModel;

import java.util.List;


public class DisplaySubVM extends ViewModel implements DisplaySubContract.ViewModel, RetrieveNextListingCallback {
  private final String TAG = "DISPLAY_SUB_VM";
  private boolean isInitialized = false;
  private String subName;
  private SubRepository subRepo;
  private PostRepository postRepo;

  private MediatorLiveData<List<PostModel>> postListMediator;
  private MediatorLiveData<SubredditModel> subMediator;

  public void init(String subredditName, SubRepository subRepo, PostRepository postRepo) {
    // ensure that the subreddit model is initialized only once
    if (!isInitialized) {
      Log.d(TAG, subredditName);
      subName = subredditName;
      this.postRepo = postRepo;
      this.subRepo = subRepo;

      // initialize the mediator for loading posts
      postListMediator = new MediatorLiveData<>();
      postListMediator.addSource(postRepo.getPosts(subName), new Observer<List<PostModel>>() {
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
      // retrieve the banner image from the subredddit css
      subRepo.getSubGateway().retrieveSubBanner(subName);

      isInitialized = true;
    }

    this.subMediator = new MediatorLiveData<>();
    this.subMediator.addSource(subRepo.getSingleSub(subName), new Observer<SubredditModel>() {
      @Override
      public void onChanged(@Nullable SubredditModel newModel) {
        if (newModel == null) {
          Log.d(TAG, "No subreddit not saved locally, retrieving from network");
          subRepo.retrieveSingleSub(subName);
        }
        else {
          Log.d(TAG, "Subreddit loaded " + newModel.getBannerUrl());
          subMediator.setValue(newModel);
        }
      }
    });

  }


  public LiveData<SubredditModel> getSubModel() {
    return subMediator;
  }


  @Override
  public String getSubName() {
    return subName;
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
      postRepo.retrieveMorePosts(subName, null);
    }
    else {
      // retrieve the "after" value for the next posting
      postRepo.getNextPostingVal(this, subName);
    }
  }


  @Override
  public void onNextListing(String nextVal) {
    Log.d(TAG, "Retrieving next posts with " + nextVal);
    // retrieve the "after" value for the next posting
    postRepo.retrieveMorePosts(subName, nextVal);
  }

}
