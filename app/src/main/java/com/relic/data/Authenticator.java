package com.relic.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

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
  final String DURATION="permanent";
  String responseType = "code";
  String state = "random0101"; // any random value
  String scope = "edit";

  Context appContext;
  RequestQueue requestQueue;

  public Authenticator(Context context) {
    appContext = context;
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
    return REDIRECT_URI;
  }
}
