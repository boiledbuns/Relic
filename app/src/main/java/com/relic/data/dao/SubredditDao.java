package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.SubredditEntity;
import com.relic.data.models.SubredditModel;

import java.util.List;

@Dao
public abstract class SubredditDao {
  @Query("SELECT id, name, bannerUrl, nsfw, subscribed, subscriberCount FROM SubredditEntity")
  public abstract List<SubredditModel> getAll();

  @Query("SELECT id, name, bannerUrl, nsfw, subscribed, subscriberCount FROM SubredditEntity ORDER BY name DESC")
  public abstract LiveData<List<SubredditModel>> getAllSubscribed();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertAll(List<SubredditEntity> subredditList);

  @Query("DELETE FROM SubredditEntity")
  public abstract void deleteAll();

  @Query("SELECT * FROM SubredditEntity WHERE name LIKE :search")
  public abstract LiveData<List<SubredditModel>> findSubreddit(String search);

  @Query("SELECT name FROM SubredditEntity WHERE name = :subName")
  public abstract LiveData<List<String>> getSubscribed(String subName);
}
