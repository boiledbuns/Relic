package com.relic.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class CommentEntity {
  @NonNull
  @PrimaryKey
  public String id;

  //public String replies;
  public String created;
  public String author;
  public String subreddit;

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


  //boolean edited;

  public CommentEntity(){}
}
