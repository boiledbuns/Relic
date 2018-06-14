package com.relic.presentation.displaypost;

import android.arch.lifecycle.ViewModel;

import com.relic.data.CommentRepository;
import com.relic.data.PostRepository;
import com.relic.data.models.PostModel;
import com.relic.presentation.callbacks.PostLoadCallback;

public class DisplayPostVM extends ViewModel implements PostLoadCallback {

  public void init(PostRepository postRepo, CommentRepository commentRepo, String fullname) {

  }

  @Override
  public void onPostLoad(PostModel post) {

  }

}
