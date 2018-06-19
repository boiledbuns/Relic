package com.relic.data.models;

public class CommentModel {
  private String id;

  public String author;
  private String body;
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

  public String getBody() {
    return body == null ? "" : body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}
