package com.relic.data;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.relic.R;

public class PostRepositoryImpl implements PostRepository {
  private final String ENDPOINT = "https://oauth.reddit.com";
  private final String ENDPOINT2 = "https://www.reddit.com";
  private final String TAG = "POST_REPO";

  private Context context;
  private String authToken;

  RequestQueue requestQueue;

  public PostRepositoryImpl(Context context) {
    this.context = context;
    requestQueue = Volley.newRequestQueue(context);

    // get the oauth token from the app's shared preferences
    String authKey = context.getResources().getString(R.string.AUTH_PREF);
    String tokenKey = context.getResources().getString(R.string.TOKEN_KEY);
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    Toast.makeText(context, "AUTH TOKEN = " + authToken, Toast.LENGTH_SHORT).show();

    getPosts();
  }


  public void getPosts() {
    String test = "/r/utsc/new.json";
    requestQueue.add(new StringRequest(Request.Method.POST, ENDPOINT2 + test,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Error: " + error.networkResponse.statusCode);
          }
        }
    ));
  }

}
