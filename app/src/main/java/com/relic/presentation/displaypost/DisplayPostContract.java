package com.relic.presentation.displaypost;

import android.arch.lifecycle.LiveData;

import com.relic.data.CommentRepository;
import com.relic.data.ListingRepository;
import com.relic.data.PostRepository;
import com.relic.data.models.CommentModel;
import com.relic.data.models.PostModel;

import java.util.List;

public interface DisplayPostContract {
  interface ViewModel {
    /**
     * Exposes the postmodel to the view
     * @return postmodel as livedata
     */
    LiveData<PostModel> getPost();

    /**
     * Exposes the postmodel to the view
     * @return postmodel as livedata
     */
    LiveData<List<CommentModel>> getCommentList();

    /**
     * Hook for view to tell the VM to retrieve more comments
     * @param refresh whether the comments should be refreshed or not
     */
    void retrieveMoreComments(boolean refresh);


    void refresh();

    void voteOnPost(String postFullname, int voteValue);
  }

  interface PostViewDelegate {
  }
}

