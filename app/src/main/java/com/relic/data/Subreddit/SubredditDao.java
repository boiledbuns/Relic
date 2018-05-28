package com.relic.data.Subreddit;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public abstract class SubredditDao {
  @Query("SELECT * FROM SubredditDecorator")
  public abstract LiveData<List<SubredditDecorator>> getAll();

  @Insert
  public abstract void insertAll(List<SubredditDecorator> subredditList);

}
