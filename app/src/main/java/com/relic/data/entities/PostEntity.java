package com.relic.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class PostEntity {
  public static final int ORIGIN_SUB = 0;
  public static final int ORIGIN_FRONTPAGE = 1;
  public static final int ORIGIN_ALL = 2;

  // api metadata
  @PrimaryKey(autoGenerate = true)
  public int order;

  // every post is associated with a listing (many posts to one listing)
  @ColumnInfo(name = "id")
  public String name;
//  public String getSubreddit_id;
//  public String postListingId;
  public boolean clicked;

  // content columns
  public String title;
  public String author;
  public String permalink;
  public String selftext;
  @ColumnInfo(name = "authorFlair")
  public String author_flair_text;
  @ColumnInfo(name = "linkFlair")
  public String link_flair_text;
  //public String media_embed;
  public int score;
  //public int likes;
  public int ups;
  public int downs;
  @ColumnInfo(name = "commentCount")
  public int num_comments;
  public String created;

  //public String media;
  //public String preview;
  public String thumbnail;
  public String domain;
  public String url;

  // post metadata
  @ColumnInfo(name = "viewCount")
  public int view_count;
  public int gilded;

  @ColumnInfo(name = "nsfw")
  public boolean over_18;

  public boolean is_video;

  @ColumnInfo(name = "self")
  public boolean is_self;
  public boolean spoiler;
  public boolean stickied;
  public boolean locked;
  public boolean archived;
  public boolean pinned;
  public boolean visited;
  public boolean saved;

  public String subreddit;
  public int userUpvoted;

  // custom local fields
  public int origin;
}
