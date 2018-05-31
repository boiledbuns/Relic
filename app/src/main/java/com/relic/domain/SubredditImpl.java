package com.relic.domain;

public class SubredditImpl implements Subreddit {
  private String id;
  private String iconLink;
  private String name;
  private boolean nsfw;

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
