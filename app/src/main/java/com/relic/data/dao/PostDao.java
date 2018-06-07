package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.PostEntity;

import java.util.List;

@Dao
public abstract class PostDao {
  @Query("SELECT  * FROM PostEntity where postListingId = :postId")
  public abstract LiveData<List<PostEntity>> getSubredditPosts(String postId);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertPosts(List<PostEntity> posts);
}
