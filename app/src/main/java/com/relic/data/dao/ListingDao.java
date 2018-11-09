package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.ListingEntity;

@Dao
public abstract class ListingDao {
  @Query("SELECT afterPosting FROM ListingEntity WHERE listingKey = :subName")
  public abstract String getNext(String subName);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract void insertListing(ListingEntity listing);

  @Query("SELECT afterPosting FROM ListingEntity WHERE listingKey = :fullName")
  public abstract LiveData<String> getAfter(String fullName);
}
