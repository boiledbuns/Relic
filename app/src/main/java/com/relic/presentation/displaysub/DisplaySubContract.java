package com.relic.presentation.displaysub;

import android.arch.lifecycle.LiveData;
import android.view.View;

import com.relic.data.PostRepository;
import com.relic.data.SubRepository;
import com.relic.data.models.PostModel;
import com.relic.data.models.SubredditModel;

import java.util.List;

public class DisplaySubContract {
  static int UPVOTE = 1;
  static int DOWNVOTE = -1;
  static int RESET = 0;

  public interface ViewModel {
    void init(String subredditName, SubRepository subRepo, PostRepository postRepo);

    /**
     * Used to check if the viewmodel has already been initialized
     * @return
     */
    boolean isInitialized();

    LiveData<SubredditModel> getSubModel();

    LiveData<List<PostModel>> getPosts();

   // LiveData<Boolean> getIsSubscribed();

    String getSubName();

    void changeSortingMethod(int sortingCode, int sortScope);

    void retrieveMorePosts(boolean resetPosts);

    void updateSubStatus(boolean subscribe);

    void visitPost(String postFullname);

    void voteOnPost(String postFullname, int voteValue);
  }
}
