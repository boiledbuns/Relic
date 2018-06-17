package com.relic.data.models;

import com.relic.domain.Post;


public class PostModel implements Post {
  private String id;
  private String subreddit;
  private String author;
  private String selftext;
  private String created;
  private int score;
  public String title;
  private int commentCount;

  public String domain;
  public String url;

  public PostModel() {}

  //  public String subName;
//  public String stringDate;
  public int getCommentCount() {
    return commentCount;
  }

  public void setCommentCount(int commentCount) {
    this.commentCount = commentCount;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSelftext() {
    return selftext;
  }

  public void setSelftext(String selftext) {
    this.selftext = selftext;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  // Only constructor should be used by room
//  public PostModel(String id, String author, int commentCount, int karma, String title,
//                   String subId, String stringDate) {
//  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getSubreddit() {
    return subreddit;
  }

  public void setSubreddit(String subreddit) {
    this.subreddit = subreddit;
  }
}
