package com.relic.data.models;

import com.relic.domain.Listing;
import com.relic.domain.Post;
import com.relic.domain.Thing;

import java.util.ArrayList;
import java.util.List;

public class PostListing implements Listing {
  private String before;
  private String after;
  private List <Post> children;

  public PostListing(String before, String after, List <Post> listings) {
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
