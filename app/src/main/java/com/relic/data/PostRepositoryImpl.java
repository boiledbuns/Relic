package com.relic.data;


import android.content.Context;

public class PostRepositoryImpl implements PostRepository{
  private Context context;

  public PostRepositoryImpl(Context context) {
    this.context = context;
  }

}
