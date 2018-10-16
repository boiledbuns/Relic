package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.relic.data.entities.SubredditEntity;
import com.relic.data.models.SubredditModel;

import java.util.List;

@Dao
public abstract class SubredditDao {
  @Query("SELECT id, name, bannerUrl, nsfw, isSubscribed, subscriberCount, description FROM SubredditEntity")
  public abstract List<SubredditModel> getAll();

  @Query("SELECT id, name, bannerUrl, nsfw, isSubscribed, subscriberCount, description, subIcon FROM SubredditEntity ORDER BY name DESC")
  public abstract LiveData<List<SubredditModel>> getAllSubscribed();

  @Query("SELECT id, name, bannerUrl, nsfw, isSubscribed, subscriberCount, description, subIcon FROM SubredditEntity " +
          "WHERE pinned = 1 ORDER BY name DESC")
  public abstract LiveData<List<SubredditModel>> getAllPinnedSubs();

  @Query("UPDATE SubredditEntity SET pinned = :pinned  WHERE name = :subredditName")
  public abstract void updatePinnedStatus(String subredditName, boolean pinned);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertAll(List<SubredditEntity> subredditList);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insert(SubredditEntity subreddit);

  @Query("DELETE FROM SubredditEntity")
  public abstract void deleteAll();

  @Query("DELETE FROM SubredditEntity WHERE isSubscribed")
  public abstract void deleteAllSubscribed();

  @Query("SELECT id, name, bannerImgUrl, nsfw, isSubscribed, subscriberCount, description, subIcon, submitText, headerTitle FROM SubredditEntity WHERE name = :subName")
  public abstract LiveData<SubredditModel> getSub(String subName);

  @Query("SELECT * FROM SubredditEntity WHERE name LIKE :search")
  public abstract LiveData<List<SubredditModel>> findSubreddit(String search);

  @Query("SELECT name FROM SubredditEntity WHERE name = :subName")
  public abstract LiveData<List<String>> getSubscribed(String subName);

  @Query("SELECT name FROM SubredditEntity WHERE name = :query")
  public abstract LiveData<List<String>> searchSubreddits(String query);

  @Query("UPDATE SubredditEntity SET isSubscribed = :subscribed WHERE name = :subredditName")
  public abstract void updateSubscription(boolean subscribed, String subredditName);

  @Query("UPDATE SubredditEntity SET headerTitle = :headerTitle, description = :description,  submitText = :submitText WHERE name = :subredditName")
  public abstract void updateSubInfo(String subredditName, String headerTitle, String description, String submitText);
}
