package com.relic.presentation.displaysub;

import com.relic.data.PostRepository;

public class DisplaySubContract {
  interface ViewModel {
    void init(PostRepository postRepo);
  }
}
