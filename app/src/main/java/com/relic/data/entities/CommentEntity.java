package com.relic.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class CommentEntity {
  public static final String MORE_CREATED = "more_author";

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  private String id;

  //public String replies;
  public String created;
  public String author;
  public String subreddit;

  // post parent id
  public String parentPostId;

  // parent comment id
  @ColumnInfo(name ="parentId")
  public String parent_id;

  @ColumnInfo(name = "body")
  public String body_html;

  public int score;

  public int platinum;
  public int gold;
  public int silver;

  @ColumnInfo(name = "authorFlairText")
  public String author_flair_text;
  @ColumnInfo(name = "authorFlairTextColor")
  public String author_flair_text_color;

  @ColumnInfo(name = "isSubmitter")
  public boolean is_submitter;
  @ColumnInfo(name = "scoreHidden")
  public boolean score_hidden;
  public int userUpvoted;
    public boolean saved;

  public int replyCount;
  public int depth;
  public String replyLink;

  public String editedDate;

  public CommentEntity(){}

  @NonNull
  public String getId() {
    return id;
  }

  public void setId(@NonNull String id) {
      this.id = id;
  }

    public float position;

  // for "more" values
     public String moreChildren;
}
