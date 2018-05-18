package com.relic.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.relic.R;

public class Authenticator {
  final String TAG = "AUTHENTICATOR";
  final String BASE = "https://www.reddit.com/api/v1/authorize.compact?";
  final String REDIRECT_URI = "https://github.com/13ABEL/Relic";
  final String DURATION="PERMANENT";
  String clientID = ""; //TODO retreieve from secret file
  String responseType = "code";
  String state = "random0101"; // any random value
  String scope = "edit";

  RequestQueue requestQueue;

  public Authenticator(Context context) {
    requestQueue = Volley.newRequestQueue(context);

    String requestURL = BASE + "client_id=" + context.getString(R.string.client_id)
        + "&response_type=" + responseType
        + "&state=" + responseType
        + "&redirect_uri=" + state
        + "&duration=" + DURATION
        + "&scope=" + scope;

    // make request to url
    StringRequest request = new StringRequest(Request.Method.GET, requestURL,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {

          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, error.getMessage());
          }
        });

  }

}
