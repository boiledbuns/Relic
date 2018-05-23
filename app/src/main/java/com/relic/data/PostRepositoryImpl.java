package com.relic.data;


import android.content.Context;
import android.widget.Toast;

import com.relic.R;

public class PostRepositoryImpl implements PostRepository{
  private Context context;
  private String authToken;

  public PostRepositoryImpl(Context context) {
    this.context = context;

    // get the oauth token from the app's shared preferences
    String authKey = context.getResources().getString(R.string.AUTH_PREF);
    String tokenKey = context.getResources().getString(R.string.TOKEN_KEY);
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    Toast.makeText(context, "AUTH TOKEN = " + authToken, Toast.LENGTH_SHORT).show();
  }



}
