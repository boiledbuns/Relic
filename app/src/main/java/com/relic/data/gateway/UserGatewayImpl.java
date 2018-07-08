package com.relic.data.gateway;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.relic.R;
import com.relic.data.Request.RedditOauthRequest;
import com.relic.data.VolleyAccessor;

public class UserGatewayImpl implements UserGateway{
  private final String ENDPOINT = "https://oauth.reddit.com/";
  public static String TAG = "USER_GATEWAY";
  private String authToken;

  RequestQueue requestQueue;

  public UserGatewayImpl(Context context) {
    // Get the key values needed to get the actual authtoken from shared preferences
    String authKey = context.getString(R.string.AUTH_PREF);
    String tokenKey = context.getString(R.string.TOKEN_KEY);

    // retrieve the authtoken for use
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    requestQueue = VolleyAccessor.getInstance(context).getRequestQueue();
  }

  public void getSelf () {

  }

  public void getUser(String username) {
    String endpoint = ENDPOINT + "/user/" + username + "/overview";
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, endpoint,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            parseUser(response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Error getting user overview");
          }
        }, authToken));
  }

  private void parseUser(String response) {
    Log.d(TAG, response);

  }


  private static class another extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
      return null;
    }
  }


}
