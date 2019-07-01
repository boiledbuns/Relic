package com.relic.persistence.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.persistence.entities.ListingEntity;

@Dao
public abstract class ListingDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertListing(ListingEntity listing);

  @Query("SELECT `after` FROM ListingEntity WHERE postSource = :sourceName")
  public abstract String getAfterString(String sourceName);
}
