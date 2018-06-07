package com.relic.data.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class PostEntity {
  // api metadata

  // every post is associated with a listing (many posts to one listing)
  @NonNull
  @PrimaryKey
  public String id;
  public String getSubreddit_id;
  public String postListingId;

  // content columns
  public String title;
  public String permalink;
  public String selftext;
  public String name;
  public String media_embed;
  public int score;
  public int likes;
  public int ups;
  public int downs;
  public int num_comments;
  public String created;

  public String media;
  public String preview;
  public String thumbnail;
  public String url;

  // post metadata
  public int view_count;
  public boolean visited;
  public boolean edited;
  public boolean over_18;
  public boolean locked;
  public boolean archived;
  public boolean is_video;
  public boolean is_self;
  public boolean spoiler;
  public String subreddit;

  public PostEntity(){

  }

}
