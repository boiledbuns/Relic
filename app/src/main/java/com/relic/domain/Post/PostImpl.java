package com.relic.domain.Post;

public class PostImpl implements Post {
  private String id;
  private String subreddit;
  private String author;

  public PostImpl(String id, String sub, String author) {
    this.id = id;
    this.subreddit = sub;
    this.author = author;
  }

  @Override
  public String toString() {
    return id + " " + subreddit + " " + author;
  }
}
