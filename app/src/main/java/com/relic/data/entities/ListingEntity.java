package com.relic.data.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Additional note: This class entity functions as a way to keep track of the most recent paging
 * "after" value to allow us to continue to load the next page based on Reddit's API structure
 */

@Entity
public class ListingEntity {
  @NonNull
  @PrimaryKey
  public String subredditName;
  public String afterPosting;

  public ListingEntity(String subredditName, String after) {
    this.subredditName = subredditName;
    this.afterPosting = after;
  }

  public ListingEntity() {}
}
