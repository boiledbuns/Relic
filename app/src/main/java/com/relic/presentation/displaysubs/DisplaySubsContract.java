package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LiveData;

import com.relic.data.SubRepository;
import com.relic.data.models.SubredditModel;
import com.relic.domain.Subreddit;

import java.util.List;

public interface DisplaySubsContract {
  interface VM {
    void init(SubRepository accountRepo);
    LiveData<List<? extends Subreddit>> getSubscribed();
  }

}
