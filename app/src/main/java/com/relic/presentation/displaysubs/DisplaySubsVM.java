package com.relic.presentation.displaysubs;

import android.arch.lifecycle.ViewModel;
import android.util.Log;
import android.widget.Toast;

import com.relic.data.SubRepository;
import com.relic.domain.Subreddit;
import com.relic.domain.behaviours.SubscribedCallback;

import java.util.List;

public class DisplaySubsVM extends ViewModel implements DisplaySubsContract.VM, SubscribedCallback {
  SubRepository subRepo;
  final String TAG = "DISPLAY_SUBS_VM";

  public void init(SubRepository accountRepo) {
    this.subRepo = accountRepo;
    subRepo.getSubscribed(this);
  }

  @Override
  public void recieveSubs(List<Subreddit> subList) {
    Log.d(TAG, subList.toString());
  }
}
