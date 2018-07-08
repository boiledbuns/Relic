package com.relic.data.Request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom String request with custom values for reddit auth
 */
public class RedditOauthRequest extends StringRequest {
  private String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private String authToken;

  public RedditOauthRequest(int method, String url, Response.Listener<String> listener,
                             Response.ErrorListener errorListener, String authToken) {
    super(method, url, listener, errorListener);
    this.authToken = authToken;
  }

  public Map<String, String> getHeaders() {
    Map <String, String> headers = new HashMap<>();

    // generate the credential string for oauth
    String credentials = "bearer " + authToken;
    headers.put("Authorization", credentials);
    headers.put("User-Agent", userAgent);

    return headers;
  }
}
