package com.relic.presentation.displaysub;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.relic.data.PostRepository;
import com.relic.data.models.PostListing;
import com.relic.data.models.SubredditModel;
import com.relic.domain.Subreddit;

import java.util.List;


public class DisplaySubVM extends ViewModel implements DisplaySubContract.ViewModel {
  private final String TAG = "DISPLAYSUB_VM";
  private SubredditModel currentSub;
  private PostRepository postRepo;

  private MediatorLiveData<List<? extends PostListing>> postListings;

  public void init(SubredditModel subModel, PostRepository postRepo) {
    Log.d(TAG, subModel.getSubName());
    this.currentSub = subModel;
    this.postRepo = postRepo;
    postRepo.getPosts("");
  }
}
