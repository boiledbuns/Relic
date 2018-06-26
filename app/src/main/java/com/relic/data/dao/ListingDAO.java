package com.relic.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.ListingEntity;

@Dao
public abstract class ListingDAO {
  @Query("SELECT afterPosting FROM ListingEntity WHERE listingKey = :subName")
  public abstract String getNext(String subName);

  //@Query("SELECT * FROM ListingEntity WHERE ")

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertListing(ListingEntity listing);
}
