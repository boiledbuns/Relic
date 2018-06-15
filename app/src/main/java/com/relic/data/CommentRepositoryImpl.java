package com.relic.data;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.relic.data.models.CommentModel;

import java.util.List;

public class CommentRepositoryImpl implements CommentRepository {
  private final String TAG = "COMMENT_REPO";
  private Context viewContext;
  private RequestQueue queue;

  public CommentRepositoryImpl (Context context) {
    //TODO convert VolleyQueue into a singleton
    queue = Volley.newRequestQueue(context);

    viewContext = context;
  }


  /**
   * Exposes the list of comments as livedata
   * @param postFullname fullname of the post to retrieve comments for
   * @return list of comments as livedata
   */
  public LiveData<List<CommentModel>> getComments(String postFullname) {


    return null;
  }


  public void retrieveComments(String postFullname) {

  }





}
