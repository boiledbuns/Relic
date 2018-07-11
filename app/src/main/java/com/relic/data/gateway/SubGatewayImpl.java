package com.relic.data.gateway;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.relic.R;
import com.relic.data.VolleyAccessor;

public class SubGatewayImpl implements SubGateway {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  public static String TAG = "USER_GATEWAY";
  private String authToken;

  RequestQueue requestQueue;

  public SubGatewayImpl(Context context) {
    // Get the key values needed to get the actual authtoken from shared preferences
    String authKey = context.getString(R.string.AUTH_PREF);
    String tokenKey = context.getString(R.string.TOKEN_KEY);

    // retrieve the authtoken for use
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    requestQueue = VolleyAccessor.getInstance(context).getRequestQueue();
  }

  //TODO include method to retrieve single subreddit, switch get all to use that instead
  public void getAdditionalSubInfo(String subredditName) {
    // get sub info and sidebar info
    

  }


  /**
   * Routes the api request through this single method based on request
   * @param action
   */
  private void router(int action, String subredditName, String subredditFullname) {

  }

}
