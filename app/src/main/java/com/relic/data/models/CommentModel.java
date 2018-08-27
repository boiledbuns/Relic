package com.relic.data.models;

public class CommentModel {
  private String id;

  public String author;
  private String body;
  public String created;
  private int score;
  private int userUpvoted;

  public boolean gilded;
  public boolean isSubmitter;


  public CommentModel() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBody() {
    return body == null ? "" : body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public int getUserUpvoted() {
    return userUpvoted;
  }

  public void setUserUpvoted(int userUpvoted) {
    this.userUpvoted = userUpvoted;
  }
}
