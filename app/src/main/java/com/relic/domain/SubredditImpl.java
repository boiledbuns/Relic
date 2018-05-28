package com.relic.domain;

public class SubredditImpl extends Subreddit {
  String id;
  String iconLink;
  String name;
  boolean nsfw;

  public SubredditImpl(String id,  String name, String iconLink, boolean nsfw) {
    this.id = id;
    this.iconLink = iconLink;
    this.name = name;
    this.nsfw = nsfw;
  }

  @Override
  public String toString() {
    return name + " " + id + " " + iconLink + " " + nsfw;
  }
}
