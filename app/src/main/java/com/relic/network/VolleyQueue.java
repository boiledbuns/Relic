package com.relic.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyQueue {
  static private final String TAG = "VOLLEY_QUEUE";
  static private RequestQueue INSTANCE;

  public static RequestQueue getQueue() {
    return INSTANCE;
  }


  /**
   * The request queue should only be called once in the application
   * @param context application context
   */
  public static RequestQueue get(Context context) {
    // initializes the instance of the request queue once!
    if (INSTANCE == null) {
      INSTANCE = Volley.newRequestQueue(context);
      Log.d(TAG, "Request queue initialized");
    }
    return INSTANCE;
  }
}
