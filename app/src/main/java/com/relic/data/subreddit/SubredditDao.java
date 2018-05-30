package com.relic.data.subreddit;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.relic.domain.Subreddit;

import java.util.List;

@Dao
public abstract class SubredditDao {
  @Query("SELECT * FROM SubredditDecorator")
  public abstract List<SubredditDecorator> getAll();

  @Insert
  public abstract void insertAll(List<SubredditDecorator> subredditList);
}
