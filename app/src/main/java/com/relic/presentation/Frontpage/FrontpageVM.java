package com.relic.presentation.Frontpage;

import android.arch.lifecycle.ViewModel;

import com.relic.data.PostRepository;

public class FrontpageVM extends ViewModel implements FrontpageContract.VM {
  private String defaultSub = "/r/frontpage/hot.json";
  PostRepository postRepo;

  public void init(PostRepository postRepository) {
    this.postRepo = postRepository;

    // get the posts for the frontpage as default
    postRepo.getPosts(defaultSub);
  }

}
