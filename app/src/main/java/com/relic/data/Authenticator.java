package com.relic.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.relic.R;

import java.util.HashMap;
import java.util.Map;

public class Authenticator {
  final String TAG = "AUTHENTICATOR";
  final String BASE = "https://www.reddit.com/api/v1/authorize.compact?";
  final String ACCESS_TOKEN_URI = "https://www.reddit.com/api/v1/access_token";
  final String REDIRECT_URI = "https://github.com/13ABEL/Relic";
  final String DURATION="permanent";
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

  public void retrieveAccessToken() {
    StringRequest stringRequest = new StringRequest(Request.Method.POST, ACCESS_TOKEN_URI,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, error.getMessage());
          }
        })
    {
      // override method to add custom headers to the request
//      @Override
//      public Map<String, String> getHeaders() throws AuthFailureError {
//        // create a new header map and add the right headers to it
//        Map<String, String> headers = new HashMap<>();
//        headers.put("grant_type","");
//        headers.put("code","CODE");
//        headers.put("redirect_uri", REDIRECT_URI);
//
//        return headers;
//      }
    };

    requestQueue.add(stringRequest);
  }


}
