package com.relic.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.relic.persistence.entities.ListingEntity;

@Dao
public abstract class ListingDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertListing(ListingEntity listing);

  @Query("SELECT `after` FROM ListingEntity WHERE postSource = :sourceName")
  public abstract String getAfterString(String sourceName);
}
