package com.relic.presentation.displaysub;

import android.arch.lifecycle.LiveData;

import com.relic.data.PostRepository;
import com.relic.data.models.PostModel;
import com.relic.data.models.SubredditModel;

import java.util.List;

public class DisplaySubContract {
  interface ViewModel {
    void init(SubredditModel subredditModel, PostRepository postRepo);

    LiveData<List<PostModel>> getPosts();

    String getSubName();

    void retrieveMorePosts(boolean resetPosts);
  }
}
