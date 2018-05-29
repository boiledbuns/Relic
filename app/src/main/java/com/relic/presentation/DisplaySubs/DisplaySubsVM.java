package com.relic.presentation.DisplaySubs;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.relic.data.SubRepository;
import com.relic.domain.Subreddit;

import java.util.List;

public class DisplaySubsVM extends ViewModel implements DisplaySubsContract.VM {
  SubRepository subRepo;
  LiveData<List<Subreddit>> subscribedSubs;

  public void init(SubRepository accountRepo) {
    this.subRepo = accountRepo;
    //subscribedSubs = subRepo.getSubscribed();
  }
}
