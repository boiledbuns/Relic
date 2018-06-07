package com.relic.data.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class PostListingEntity {
  @PrimaryKey
  public String id;
  public String subredditID;
  public String before;
  public String after;
}
