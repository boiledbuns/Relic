package com.relic.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.relic.R;

import java.util.HashMap;
import java.util.Map;

public class AccountRepositoryImpl implements AccountRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "ACCOUNT_REPO";

  private Context context;

  public AccountRepositoryImpl(Context context) {
    Authenticator auth = new Authenticator(context);
    auth.refreshToken();

    this.context = context;
    getMe();

  }

  private void getMe() {
    VolleyQueue.getQueue().add(
        new StringRequest(
            Request.Method.GET, ENDPOINT + "subreddits/mine/subscriber",
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                parseUser(response);
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error : " + error.networkResponse.headers.toString());
              }
            }
    ) {
        public Map<String, String> getHeaders() {
          Map <String, String> headers = new HashMap<>();

          String auth = context.getSharedPreferences(
              context.getResources().getString(R.string.AUTH_PREF),
              Context.MODE_PRIVATE)
              .getString(context.getResources().getString(R.string.TOKEN_KEY), "DEFAULT");


          // generate the credential string for oauth
          String credentials = "bearer " + auth;
          headers.put("Authorization", credentials);
          headers.put("User-Agent", userAgent);

          return headers;
        }
    });
  }

  private void parseUser(String response) {
    Log.d(TAG, response);
  }


}
