package com.relic.domain;

import java.util.List;

public class ListingImpl implements Listing {
  private String before;
  private String after;
  private List <Thing> children;

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
    return children;
  }
}
