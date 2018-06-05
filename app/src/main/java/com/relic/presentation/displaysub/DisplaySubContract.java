package com.relic.presentation.displaysub;

import com.relic.data.PostRepository;
import com.relic.data.models.SubredditModel;

public class DisplaySubContract {
  interface ViewModel {
    void init(SubredditModel subredditModel, PostRepository postRepo);
  }
}
