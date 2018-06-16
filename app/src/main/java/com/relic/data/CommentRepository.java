package com.relic.data;

import android.arch.lifecycle.LiveData;

import com.relic.data.models.CommentModel;
import com.relic.data.models.PostModel;

import java.util.List;

public interface CommentRepository {


  LiveData<List<CommentModel>> getComments(String postFullname);


  void retrieveComments(String subName, String postFullname, String after);

}
