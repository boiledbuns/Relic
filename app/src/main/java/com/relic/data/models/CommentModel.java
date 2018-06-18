package com.relic.data.models;

public class CommentModel {
  private String id;

  public String author;
  public String body;
  public String created;
  public int score;

  public boolean gilded;
  public boolean isSubmitter;

  public CommentModel() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
