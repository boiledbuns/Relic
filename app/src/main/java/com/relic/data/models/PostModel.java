package com.relic.data.models;

import android.arch.persistence.room.Ignore;

import com.relic.domain.Post;


public class PostModel implements Post {
  private String id;
  public String title;
  private int commentCount;

  @Ignore
  private int karma;

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



  // Only constructor should be used by room
//  public PostModel(String id, String author, int commentCount, int karma, String title,
//                   String subId, String stringDate) {
//  }

  public PostModel() {}

}
