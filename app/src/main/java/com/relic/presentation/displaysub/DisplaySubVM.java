package com.relic.presentation.displaysub;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;

import com.relic.data.PostRepository;
import com.relic.data.models.PostListing;

import java.util.List;


public class DisplaySubVM extends ViewModel implements DisplaySubContract.ViewModel {
  private PostRepository postRepo;
  private MediatorLiveData<List<? extends PostListing>> postListings;

  public void init(PostRepository postRepo) {
    this.postRepo = postRepo;
    postRepo.getPosts("");
  }




}
