package com.relic.presentation.Frontpage;

import android.arch.lifecycle.ViewModel;

import com.relic.data.PostRepository;

public class FrontpageVM extends ViewModel implements FrontpageContract.VM {
  PostRepository postRepo;

  public void init(PostRepository postRepository) {
    this.postRepo = postRepository;
  }

}
