package com.relic.data.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.relic.domain.Subreddit;


/**
 * Mapper class with public var to reduce boilerplate and and allow the SubredditImpl class to be
 * used with room without violating clean architecture (ie. keeping it framework independent)
 */
@Entity
public class SubredditModel implements Subreddit {
  @NonNull
  @PrimaryKey
  public String id;
  public String name;
  public String iconLink;
  public boolean nsfw;

  public SubredditModel(String id, String name, String iconLink, boolean nsfw) {
    this.id = id;
    this.iconLink = iconLink;
    this.name = name;
    this.nsfw = nsfw;
  }

  @Override
  public String toString() {
    return name + " " + id + " " + iconLink + " " + nsfw;
  }

//  public SubredditImpl mapToDomain() {
//    return new SubredditImpl(id, iconLink, name, nsfw);
//  }
}
