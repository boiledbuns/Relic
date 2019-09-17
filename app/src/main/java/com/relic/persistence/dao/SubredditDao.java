package com.relic.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.relic.domain.models.SubredditModel;

import java.util.List;

@Dao
public abstract class SubredditDao {
  @Query("SELECT * FROM SubredditModel")
  public abstract List<SubredditModel> getAll();

  @Query("SELECT * FROM SubredditModel ORDER BY subName DESC")
  public abstract LiveData<List<SubredditModel>> getAllSubscribed();

  @Query("SELECT * FROM SubredditModel " +
          "WHERE pinned = 1 ORDER BY subName DESC")
  public abstract LiveData<List<SubredditModel>> getAllPinnedSubs();

  @Query("UPDATE SubredditModel SET pinned = :pinned  WHERE subName = :subredditName")
  public abstract void updatePinnedStatus(String subredditName, boolean pinned);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertAll(List<SubredditModel> subredditList);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insert(SubredditModel subreddit);

  @Query("DELETE FROM SubredditModel WHERE pinned != 1")
  public abstract void deleteAll();

  @Query("DELETE FROM SubredditModel WHERE isSubscribed AND pinned != 1")
  public abstract void deleteAllSubscribed();

  @Query("SELECT * FROM SubredditModel WHERE subName = :subName")
  public abstract LiveData<SubredditModel> getSub(String subName);

  @Query("SELECT * FROM SubredditModel WHERE subName LIKE :search")
  public abstract LiveData<List<SubredditModel>> findSubreddit(String search);

  @Query("SELECT subName FROM SubredditModel WHERE subName = :subName")
  public abstract LiveData<List<String>> getSubscribed(String subName);

  @Query("SELECT subName FROM SubredditModel WHERE subName = :query")
  public abstract LiveData<List<String>> searchSubreddits(String query);

  @Query("UPDATE SubredditModel SET isSubscribed = :subscribed WHERE subName = :subredditName")
  public abstract void updateSubscription(boolean subscribed, String subredditName);

  @Query("UPDATE SubredditModel SET headerTitle = :headerTitle, description = :description,  submitText = :submitText WHERE subName = :subredditName")
  public abstract void updateSubInfo(String subredditName, String headerTitle, String description, String submitText);
}
