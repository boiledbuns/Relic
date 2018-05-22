package com.relic.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.relic.R;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Authenticator {
  final String TAG = "AUTHENTICATOR";
  final String BASE = "https://www.reddit.com/api/v1/authorize.compact?";
  final String ACCESS_TOKEN_URI = "https://www.reddit.com/api/v1/access_token";
  final String REDIRECT_URI = "https://github.com/13ABEL/Relic";
  final String DURATION="permanent";

  final String PREFERENCE = "auth";
  final String TOKEN_KEY = "access_token";


  String responseType = "code";
  String state = "random0101"; // any random value
  String scope = "edit";

  Context appContext;
  RequestQueue requestQueue;

  public Authenticator(Context context) {
    appContext = context;
    requestQueue = Volley.newRequestQueue(context);
    // make request to url
  }

  public String getUrl() {
    return BASE + "client_id=" + appContext.getString(R.string.client_id)
        + "&response_type=" + responseType
        + "&state=" + state
        + "&redirect_uri=" + REDIRECT_URI
        + "&duration=" + DURATION
        + "&scope=" + scope;
  }

  public String getRedirect() {
    return this.REDIRECT_URI;
  }



  public void retrieveAccessToken(String redirectUrl) {
    String queryStrings = redirectUrl.substring(REDIRECT_URI.length() + 1);
    String[] queryPairs = queryStrings.split("&");

    final Map<String, String> queryMap = new HashMap<>();
    String key = "";
    for (String queryPair : queryPairs) {
      String[] mapping = queryPair.split("=");
      queryMap.put(mapping[0], mapping[1]);
    }
    Log.d(TAG, queryMap.keySet().toString() + " " + queryMap.get("code"));

    RedditGetTokenRequest req = new RedditGetTokenRequest(Request.Method.POST, ACCESS_TOKEN_URI,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, response);
            saveReturn(response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, error.toString());
          }
        },
        queryMap.get("code"));

    requestQueue.add(req);
  }

  /**
   * checks if the user is currently signed in by checking shared preferences
   * @return whether the user is signed in
   */
  public boolean isAuthenticated() {
    return appContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
        .contains(TOKEN_KEY);
  }

  private void saveReturn(String response) {
    JSONParser parser = new JSONParser();
    try {
      JSONObject data = (JSONObject) parser.parse(response);
      // stores the token in shared preferences
      appContext.getSharedPreferences("auth", Context.MODE_PRIVATE).edit()
          .putString(TOKEN_KEY, (String) data.get(TOKEN_KEY)).apply();

      Log.d(TAG, "token saved!");
    } catch (ParseException e) {
      Toast.makeText(appContext, "yikes", Toast.LENGTH_SHORT).show();
    }
  }


  class RedditGetTokenRequest extends StringRequest {
    private String redirectCode;
    private RedditGetTokenRequest(int method, String url, Response.Listener<String> listener,
                                 Response.ErrorListener errorListener, String redirectCode) {

      super(method, url, listener, errorListener);
      this.redirectCode = redirectCode;
    }

    // override headers to add custom credentials in client_secret:redirect_code format
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
      // create a new header map and add the right headers to it
      Map<String, String> headers = new HashMap<>();

      // generate encoded credential string with client id and code from redirect
      String credentials = appContext.getString(R.string.client_id) + ":" + redirectCode;
      String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
      headers.put("Authorization", auth);

      return headers;
    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError {
      Map<String, String> params = new HashMap<>();

      params.put("grant_type", "authorization_code");
      params.put("code", redirectCode);
      params.put("redirect_uri", REDIRECT_URI);

      return params;
    }
  }

}
