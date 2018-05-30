package com.relic.data.listing;

import com.relic.domain.post.Post;
import com.relic.domain.Thing;

import java.util.ArrayList;
import java.util.List;

public class PostListing extends Listing {
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
    return new ArrayList<Thing>(children);
  }
}
