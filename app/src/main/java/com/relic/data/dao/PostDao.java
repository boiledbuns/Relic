package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.PostEntity;
import com.relic.data.models.PostModel;

import java.util.List;

@Dao
public abstract class PostDao {
  @Query("SELECT id, title, author, selftext, score, commentCount, created, subreddit, domain, url, thumbnail FROM PostEntity WHERE subreddit = :subName")
  public abstract LiveData<List<PostModel>> getSubredditPosts(String subName);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertPosts(List<PostEntity> posts);

  @Query("DELETE FROM PostEntity WHERE subreddit = :subName")
  public abstract void deleteAllFromSub(String subName);

  @Query("SELECT id, title, author, selftext, score, commentCount, created, thumbnail, url FROM PostEntity WHERE id = :postName")
  public abstract LiveData<PostModel> getSinglePost(String postName);
}
