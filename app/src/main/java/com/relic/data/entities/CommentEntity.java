package com.relic.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class CommentEntity {
  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  private String id;

  //public String replies;
  public String created;
  public String author;
  public String subreddit;

  // post id for comments in  s
  @ColumnInfo(name ="parentId")
  public String parent_id;

  @ColumnInfo(name = "body")
  public String body_html;

  public int score;
  @ColumnInfo(name = "gilded")
  public int gilded;

  @ColumnInfo(name = "isSubmitter")
  public boolean is_submitter;
  @ColumnInfo(name = "scoreHidden")
  public boolean score_hidden;
  public int userUpvoted;

  public int replyCount;
  public int depth;
  public String edited;

  //boolean edited;

  public CommentEntity(){}

  @NonNull
  public String getId() {
    return id;
  }

  public void setId(@NonNull String id) {
      this.id = id;
  }
}
