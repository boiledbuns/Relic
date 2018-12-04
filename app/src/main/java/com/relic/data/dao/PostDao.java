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
  // TODO : Currently can't use constants from entity class in query annotation so hardcoded for now

  @Query("SELECT * FROM PostEntity WHERE subreddit = :subName AND origin = 0 ORDER BY `order` ASC")
  public abstract LiveData<List<PostModel>> getSubredditPosts(String subName);

  @Query("SELECT * FROM PostEntity WHERE origin = :postOrigin ORDER BY `order` ASC")
  public abstract LiveData<List<PostModel>> getPostsFromOrigin(int postOrigin);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertPosts(List<PostEntity> posts);

  @Query("DELETE FROM PostEntity WHERE subreddit = :subName")
  public abstract void deleteAllFromSub(String subName);

  @Query("DELETE FROM PostEntity WHERE origin = :sourceCode")
  public abstract void deleteAllFromSource(int sourceCode);

  @Query("SELECT * FROM PostEntity WHERE id = :postName")
  public abstract LiveData<PostModel> getSinglePost(String postName);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertPost(PostEntity post);

  @Query("UPDATE PostEntity SET visited = 1 where id = :postFullname")
  public abstract void updateVisited(String postFullname);

  @Query("UPDATE PostEntity SET userUpvoted = :vote  where id = :postFullname")
  public abstract void updateVote(String postFullname, int vote);

  @Query("UPDATE PostEntity SET saved = :saved  where id = :postFullname")
  public abstract void updateSave(String postFullname, boolean saved);
}
