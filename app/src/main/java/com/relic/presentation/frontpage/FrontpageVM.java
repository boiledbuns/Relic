package com.relic.presentation.frontpage;

import android.arch.lifecycle.ViewModel;

import com.relic.data.auth.AuthImpl;
import com.relic.data.PostRepository;

public class FrontpageVM extends ViewModel implements FrontpageContract.VM {
  private String defaultSub = "";
  private PostRepository postRepo;
  private AuthImpl authenticator;

  public void init(PostRepository postRepository, AuthImpl auth) {
    this.postRepo = postRepository;
    this.authenticator = auth;

    // get the posts for the frontpage as default
    //    auth.refreshToken(this);
//    postRepo.getPosts(defaultSub);
  }

}
