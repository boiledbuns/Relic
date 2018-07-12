package com.relic.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class SubredditEntity {
  @NonNull
  @PrimaryKey
  public String id;

  public String fullName;
  @ColumnInfo(name = "description")
  public String public_description;
  @ColumnInfo(name = "descriptionHtml")
  public String public_description_html;
  public String user_is_subscriber;
  //public String name;

  @ColumnInfo(name = "name")
  public String display_name;

  @ColumnInfo(name = "bannerUrl")
  public String banner_url;
  @ColumnInfo(name = "iconUrl")
  public String icon_img;

  public int subscribers;

  @ColumnInfo(name = "nsfw")
  public boolean over18;
  public boolean show_media;
  public boolean user_is_banned;
  public boolean user_is_moderator;
}
