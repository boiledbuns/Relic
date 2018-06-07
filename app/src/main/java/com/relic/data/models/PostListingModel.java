package com.relic.data.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.relic.domain.Listing;
import com.relic.domain.Post;
import com.relic.domain.Thing;

import java.util.ArrayList;
import java.util.List;

@Entity
public class PostListingModel implements Listing {
  @PrimaryKey
  private int listingID;
  private String before;
  private String after;
  private List <Post> children;

  public PostListingModel(String before, String after, List <Post> listings) {
    this.before = before;
    this.after = after;
    this.children = listings;
  }

  @Override
  public String getBefore() {
    return before;
  }

  @Override
  public String getNext() {
    return after;
  }

  @Override
  public List<Thing> getChildren() {
    return new ArrayList<Thing>();
  }
}
