//package com.relic.persistence.entities;
//
//import android.arch.persistence.room.ColumnInfo;
//import android.arch.persistence.room.Entity;
//import android.arch.persistence.room.PrimaryKey;
//import android.support.annotation.NonNull;
//
//@Entity
//public class SubredditModel {
//  // reddit subreddit api properties region
//
//  @NonNull
//  @PrimaryKey
//  public String id;
//
//  public String fullName;
//
//  @ColumnInfo(name = "bannerImgUrl")
//  public String banner_img;
//  @ColumnInfo(name = "subIcon")
//  public String community_icon;
//
//  @ColumnInfo(name = "isSubscribed")
//  public boolean user_is_subscriber;
//  //public String fullname;
//
//  @ColumnInfo(name = "fullname")
//  public String display_name;
//
//  @ColumnInfo(name = "bannerUrl")
//  public String banner_background_image;
//  @ColumnInfo(name = "iconUrl")
//  public String icon_img;
//
//  @ColumnInfo(name = "subscriberCount")
//  public int subscribers;
//
//  @ColumnInfo(name = "nsfw")
//  public boolean over18;
//  public boolean show_media;
//  public boolean user_is_banned;
//  public boolean user_is_moderator;
//
//  // Subreddit information
//  @ColumnInfo(name = "description")
//  public String public_description;
//  @ColumnInfo(name = "descriptionHtml")
//  public String public_description_html;
//
//  @ColumnInfo(name = "submitText")
//  public String submit_text;
//
//  @ColumnInfo(name = "headerTitle")
//  public String header_title;
//
//  // end reddit subreddit api properties region
//
//  // local properties region
//  public boolean pinned;
//}
