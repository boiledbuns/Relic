package com.relic.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Mapper class with public var to reduce boilerplate and and allow the SubredditImpl class to be
 * used with room without violating clean architecture (ie. keeping it framework independent)
 */
public class SubredditModel implements  Parcelable {
  public String id;
  public String name;
  public String bannerUrl;
  public String bannerImgUrl;
  public boolean nsfw;
  private boolean isSubscribed;
  private int subscriberCount;
  private String subIcon;
  private String description;
  private String submitText;
  private String headerTitle;

  public SubredditModel () { }

  public SubredditModel(String id, String name, String bannerUrl, boolean nsfw) {
    this.id = id;
    this.bannerUrl = bannerUrl;
    this.name = name;
    this.nsfw = nsfw;
  }

  @Override
  public String toString() {
    return name + " " + id + " " + bannerUrl + " " + nsfw;
  }

  public String getId() {
    return id;
  }

  public String getSubName() {
    return name;
  }

  public String getBannerUrl() {
    return bannerImgUrl;
  }

  public int describeContents() {
    return 0;
  }

  /**
   * Writes relevant object properties to the parcel
   * @param dest
   * @param flags
   */
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(name);
    dest.writeString(bannerUrl);
    dest.writeInt(nsfw ? 1 : 0);
  }


  private SubredditModel(Parcel source) {
    this.id = source.readString();
    this.bannerUrl = source.readString();
    this.name = source.readString();

    this.nsfw = source.readInt() == 1;
  }

  public static final Parcelable.Creator<SubredditModel> CREATOR
      = new Parcelable.Creator<SubredditModel> () {
      @Override
      public SubredditModel createFromParcel(Parcel source) {
        return new SubredditModel(source);
      }

      @Override
      public SubredditModel[] newArray(int size) {
        return new SubredditModel[0];
      }
  };

  public boolean getIsSubscribed() {
    return isSubscribed;
  }

  public void setIsSubscribed(boolean subscribed) {
    this.isSubscribed = subscribed;
  }

  public int getSubscriberCount() {
    return subscriberCount;
  }

  public void setSubscriberCount(int subscriberCount) {
    this.subscriberCount = subscriberCount;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSubIcon() {
    return subIcon;
  }

  public void setSubIcon(String subIcon) {
    this.subIcon = subIcon;
  }

  public void setHeaderTitle(String headerTitle) {
    this.headerTitle = headerTitle;
  }

  public String getSubmitText() {
    return submitText;
  }

  public void setSubmitText(String submitText) {
    this.submitText = submitText;
  }

  public String getHeaderTitle() {
    return headerTitle;
  }
}
