package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.PostEntity;
import com.relic.data.models.PostModel;

import java.util.List;

@Dao
public abstract class PostDao {
  @Query("SELECT id, title, commentCount  FROM PostEntity where postListingId != :postListingId")
  public abstract LiveData<List<PostModel>> getSubredditPosts(String postListingId);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertPosts(List<PostEntity> posts);
}
