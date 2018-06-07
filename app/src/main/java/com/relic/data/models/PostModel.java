package com.relic.data.models;

import com.relic.domain.Account;
import com.relic.domain.Post;


public class PostModel implements Post {
  String id;

  Account author;
  public int commentCount;
  public int karma;

  public String title;
  public String subName;
  public String stringDate;

  public PostModel(String id, String author, int commentCount, int karma, String title,
                   String subId, String stringDate) {
  }


}
