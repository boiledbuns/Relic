package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.ListingEntity;

@Dao
public abstract class ListingDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertListing(ListingEntity listing);

  @Query("SELECT `after` FROM ListingEntity WHERE postSource = :sourceName")
  public abstract String getAfterString(String sourceName);
}
