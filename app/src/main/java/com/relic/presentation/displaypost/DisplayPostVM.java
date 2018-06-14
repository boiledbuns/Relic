package com.relic.presentation.displaypost;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.relic.data.CommentRepository;
import com.relic.data.PostRepository;
import com.relic.data.models.PostModel;

public class DisplayPostVM extends ViewModel implements DisplayPostContract.ViewModel {
  private PostRepository postRepo;
  private CommentRepository commentRepo;

  private LiveData<PostModel> currentPost;


  public void init(PostRepository postRepo, CommentRepository commentRepo, String fullname) {
    // initialize reference to repos for this VM
    this.postRepo = postRepo;
    this.commentRepo = commentRepo;

    // retrieves the livedata post to be exposed to the view
    currentPost = postRepo.getPost(fullname);
  }

  public LiveData<PostModel> getPost() {
    return currentPost;
  }

}
