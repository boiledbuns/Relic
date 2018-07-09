package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.models.SubredditModel;

import java.util.List;

@Dao
public abstract class SubredditDao {
  @Query("SELECT * FROM SubredditModel")
  public abstract List<SubredditModel> getAll();

  @Query("SELECT * FROM SubredditModel ORDER BY name DESC")
  public abstract LiveData<List<SubredditModel>> getAllSubscribed();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertAll(List<SubredditModel> subredditList);

  @Query("DELETE FROM SubredditModel")
  public abstract void deleteAll();

  @Query("SELECT * FROM SubredditModel WHERE name LIKE :search")
  public abstract LiveData<List<SubredditModel>> findSubreddit(String search);

}
