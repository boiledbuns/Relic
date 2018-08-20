package com.relic.data.gateway;

import android.arch.lifecycle.LiveData;

public interface PostGateway {
  LiveData<Boolean> voteOnPost(String fullname, int voteStatus);

  LiveData<Boolean> savePost(String fullname, boolean saved);

  LiveData<Boolean> comment(String fullname, String comment);

  LiveData<Boolean> gildPost(String fullname, boolean gild);

  LiveData<Boolean> reportPosts(String fullname, boolean report);

  LiveData<Boolean> visitPost(String postFullname);
}
