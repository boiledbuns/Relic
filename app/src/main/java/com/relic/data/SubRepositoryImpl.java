package com.relic.data;

import android.content.Context;

import com.relic.data.Subreddit.SubredditDB;

public class SubRepositoryImpl implements SubRepository {
//  private SubRepositoryImpl INSTANCE;
//
//  private SubRepositoryImpl() {
//
//  }
//
//  public SubRepositoryImpl getSubRepository() {
//    if (INSTANCE == null) {
//      INSTANCE = new SubRepositoryImpl();
//    }
//    return INSTANCE;
//  }
  private SubredditDB subDB;

  public SubRepositoryImpl(Context context) {
    subDB = SubredditDB.getDatabase(context);
  }

}
