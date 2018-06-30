package com.relic.data;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyAccessor {
  private static VolleyAccessor INSTANCE;
  private RequestQueue requestQueue;
  private static Context applicationContext;


  /**
   * private singleton constructor
   * @param context context of the application
   */
  private VolleyAccessor(Context context) {
    // initialize the application context and request queue
    applicationContext = context;
    requestQueue = getRequestQueue();
  }


  /**
   * Accessor to singleton instance
   * @param context
   * @return INSTANCE of Volley Accessor
   */
  public static synchronized VolleyAccessor getInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = new VolleyAccessor(context);
    }
    return INSTANCE;
  }


  /**
   *
   * @return
   */
  public RequestQueue getRequestQueue() {
    if (requestQueue == null) {
      // initialize the request queue with application context
      requestQueue = Volley.newRequestQueue(applicationContext.getApplicationContext());
    }
    return requestQueue;
  }
}
