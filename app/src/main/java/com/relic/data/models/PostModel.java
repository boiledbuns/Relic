package com.relic.data.models;

import android.text.Html;

import com.relic.domain.Post;

public class PostModel extends ListingItem implements Post {
    public static String TYPE = "t3";
    private String author;
  private String selftext;
  private String created;
  private String linkFlair;
  private int score;
  public String title;
  private int commentCount;

  private String domain;
  private String url;
  private String thumbnail;

  private String authorFlair;

  private boolean nsfw;
  private boolean stickied;
  private boolean pinned;
  private boolean locked;
  private boolean archived;
  private int viewCount;


  public PostModel() {}

  //  public String subName;
//  public String stringDate;
  public int getCommentCount() {
    return commentCount;
  }

  public void setCommentCount(int commentCount) {
    this.commentCount = commentCount;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSelftext() {
    return selftext == null ? "" : selftext;
  }

  public void setSelftext(String selftext) {
    this.selftext = selftext;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  // Only constructor should be used by room
//  public PostModel(String id, String author, int commentCount, int karma, String title,
//                   String subId, String stringDate) {
//  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getSubreddit() {
    return subreddit;
  }

  public void setSubreddit(String subreddit) {
    this.subreddit = subreddit;
  }

  public String getHtmlSelfText() {
    return selftext == null ? selftext: Html.fromHtml(Html.fromHtml(selftext).toString()).toString();
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    if (thumbnail != null && thumbnail.equals("self")) {
      this.thumbnail = null;
    } else {
      this.thumbnail = thumbnail;
    }
  }

  public boolean isNsfw() {
    return nsfw;
  }

  public void setNsfw(boolean nsfw) {
    this.nsfw = nsfw;
  }

  public boolean isStickied() {
    return stickied;
  }

  public void setStickied(boolean stickied) {
    this.stickied = stickied;
  }

  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  public boolean isPinned() {
    return pinned;
  }

  public void setPinned(boolean pinned) {
    this.pinned = pinned;
  }

  public String getAuthorFlair() {
    return authorFlair;
  }

  public void setAuthorFlair(String authorFlair) {
    this.authorFlair = authorFlair;
  }

  public int getViewCount() {
    return viewCount;
  }

  public void setViewCount(int viewCount) {
    this.viewCount = viewCount;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getLinkFlair() {
    return linkFlair;
  }

  public void setLinkFlair(String linkFlair) {
    this.linkFlair = linkFlair;
  }

  public int platinum;
  public int gold;
  public int silver;
}
