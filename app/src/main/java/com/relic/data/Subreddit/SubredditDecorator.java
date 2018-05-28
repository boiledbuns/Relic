package com.relic.data.Subreddit;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.relic.domain.SubredditImpl;

/**
 * Mapper class with public var to reduce boilerplater and and allow the SubredditImpl class to be
 * used with room without violating clean architecture (ie. keeping it framework independent)
 */
@Entity
public class SubredditDecorator {
  @NonNull
  @PrimaryKey
  public String id;
  public String name;
  public String iconLink;
  public boolean nsfw;

  public SubredditDecorator(String id, String name, String iconLink, boolean nsfw) {
    this.id = id;
    this.iconLink = iconLink;
    this.name = name;
    this.nsfw = nsfw;
  }

  public SubredditImpl mapToDomain() {
    return new SubredditImpl(id, iconLink, name, nsfw);
  }


}
