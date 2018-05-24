package com.relic.data;


import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.relic.R;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class PostRepositoryImpl implements PostRepository {
  private final String ENDPOINT = "https://oauth.reddit.com";
  private final String ENDPOINT2 = "https://www.reddit.com";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "POST_REPO";

  private Context context;
  private String authToken;
  private JSONParser JSONParser;

  RequestQueue requestQueue;

  public PostRepositoryImpl(Context context) {
    this.context = context;
    requestQueue = Volley.newRequestQueue(context);
    JSONParser = new JSONParser();

    // get the oauth token from the app's shared preferences
    String authKey = context.getResources().getString(R.string.AUTH_PREF);
    String tokenKey = context.getResources().getString(R.string.TOKEN_KEY);
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    Toast.makeText(context, "AUTH TOKEN = " + authToken, Toast.LENGTH_SHORT).show();
  }


  public void getPosts(String subreddit) {
    // create the new request and submit it
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, ENDPOINT + subreddit,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, response);
            try {
              parsePosts(response);
            }
            catch (ParseException error) {
              Log.d(TAG, "Error: " + error.getMessage());
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Error: " + error.networkResponse.headers.toString());
          }
        }
    ));
  }

  private void parsePosts(String response) throws ParseException {
    JSONObject object = (JSONObject) JSONParser.parse(response);

  }


  class RedditOauthRequest extends StringRequest {
    public RedditOauthRequest(int method, String url, Response.Listener<String> listener,
                              Response.ErrorListener errorListener) {
      super(method, url, listener, errorListener);
    }

    public Map<String, String> getHeaders() {
      Map <String, String> headers = new HashMap<>();

      // generate the credential string for oauth
      String credentials = "bearer " + authToken;
      Log.d(TAG, credentials);
      // String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

      headers.put("Authorization", credentials);
      headers.put("User-Agent", userAgent);

      return headers;
    }

  }
}
