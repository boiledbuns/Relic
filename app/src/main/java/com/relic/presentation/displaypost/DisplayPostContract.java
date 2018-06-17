package com.relic.presentation.displaypost;

import android.arch.lifecycle.LiveData;

import com.relic.data.CommentRepository;
import com.relic.data.PostRepository;
import com.relic.data.models.CommentModel;
import com.relic.data.models.PostModel;

import java.util.List;

public interface DisplayPostContract {
  interface ViewModel {
    /**
     * initialize and inject dependencies into the viewmodel
     * @param postRepo repository for retrieving posts
     * @param commentRepo repository for retrieving comments
     * @param fullName the fullname of the post to be displayed
     */
    void init(PostRepository postRepo, CommentRepository commentRepo, String subreddit, String fullName);


    /**
     * Exposes the postm to the view
     * @return postmodel as livedata
     */
    LiveData<PostModel> getPost();


    /**
     * Exposes the postm to the view
     * @return postmodel as livedata
     */
    LiveData<List<CommentModel>> getCommentList();
  }
}
