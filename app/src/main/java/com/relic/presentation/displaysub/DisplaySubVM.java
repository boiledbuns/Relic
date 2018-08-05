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
  private MutableLiveData<Boolean> subscribed;

  public void init(String subredditName, SubRepository subRepo, PostRepository postRepo) {
    // ensure that the subreddit model is reinitialized when the subreddit changes
    if (!subredditName.equals(subName)) {
      Log.d(TAG, "VM to display " + subredditName + " initialized");
      subName = subredditName;
      this.postRepo = postRepo;
      this.subRepo = subRepo;

      // initialize observables
      subscribed = new MutableLiveData<>();
      postListMediator = new MediatorLiveData<>();
      subMediator = new MediatorLiveData<>();


      postListMediator.addSource(postRepo.getPosts(subName), new Observer<List<PostModel>>() {
        @Override
        public void onChanged(@Nullable List<PostModel> postModels) {
          if (postModels != null && postModels.isEmpty()) {
            Log.d(TAG, "empty posts");
            retrieveMorePosts(true);
          } else {
            Log.d(TAG, "not empty posts");
            postListMediator.setValue(postModels);
          }
        }
      });

      // TODO: STILL TESTING retrieve the banner image from the subredddit css
      //subRepo.getSubGateway().retrieveSubBanner(subName);
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
            // update subscribed observable
            subscribed.setValue(newModel.getIsSubscribed());
          }
        }
      });

      isInitialized = true;
    }
  }

  @Override
  public boolean isInitialized() {
    return isInitialized;
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


  public void subscribeToSub() {
    MediatorLiveData<Boolean> successLive = new MediatorLiveData<>();
    successLive.addSource(subRepo.getSubGateway().subscribe(subName), (Boolean success) -> {
      if (success != null) {
        successLive.setValue(success);

        //subscribed.setValue(success);

        // unsubscribe after consuming event

      }
    });
  }

}
