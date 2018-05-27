package com.relic.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

/**
 * Singleton instance of the authenticator because we should be able to
 */
public class Authenticator {
  private final String TAG = "AUTHENTICATOR";
  private final String BASE = "https://www.reddit.com/api/v1/authorize.compact?";
  private final String ACCESS_TOKEN_URI = "https://www.reddit.com/api/v1/access_token";
  private final String REDIRECT_URI = "https://github.com/13ABEL/Relic";
  private final String DURATION="permanent";

  private String preference;
  private String tokenKey;
  private String refreshTokenKey;
  private String redirectCode;

  private String responseType = "code";
  private String state = "random0101"; // any random value
  private String scope = "identity account mysubreddits edit flair history modconfig modflair modlog modposts modwiki" +
      " mysubreddits privatemessages read report save submit subscribe vote wikiedit wikiread";

  Context appContext;
  RequestQueue requestQueue;

  private static Authenticator INSTANCE;

  /**
   * Private constructor to initialize the single instance of the Authenticator
   * @param context
   */
  public Authenticator(Context context) {
    appContext = context;
    requestQueue = VolleyQueue.getQueue();
    // retrieve the strings from res
    preference = context.getResources().getString(R.string.AUTH_PREF);
    tokenKey = context.getResources().getString(R.string.TOKEN_KEY);
    refreshTokenKey = context.getResources().getString(R.string.REFRESH_TOKEN_KEY);
    redirectCode = context.getString(R.string.REDIRECT_CODE);
  }


  public static Authenticator getAuthenticator(Context context) {
    if (INSTANCE == null) {
      // initialize the instance of the authenticator if it's null
      INSTANCE = new Authenticator(context);
    }
    return INSTANCE;
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


  /**
   * Callback used to parse the url after the user has authenticated through the reddit auth page.
   * Retrieves the code value and uses it to obtain the real auth token
   * @param redirectUrl url with params to parse
   */
  public void retrieveAccessToken(String redirectUrl) {
    String queryStrings = redirectUrl.substring(REDIRECT_URI.length() + 1);
    String[] queryPairs = queryStrings.split("&");

    // parses the redirect to get the access token needed to retrieve the access token
    final Map<String, String> queryMap = new HashMap<>();
    for (String queryPair : queryPairs) {
      String[] mapping = queryPair.split("=");
      queryMap.put(mapping[0], mapping[1]);
    }
    // stores the redirect "code" in shared preferences for easy access
    Log.d(TAG, queryMap.keySet().toString() + " " + queryMap.get(redirectCode));
    appContext.getSharedPreferences(preference, Context.MODE_PRIVATE).edit()
        .putString(redirectCode, queryMap.get(redirectCode)).apply();

    // get the access and refresh token
    requestQueue.add(new RedditGetTokenRequest(Request.Method.POST, ACCESS_TOKEN_URI,
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
        })
    );
  }


  /**
   * Refreshes the current access token using the refresh token to get a permanent
   * auth token that can be used forever
   */
  public void refreshToken() {
    requestQueue.add(new RedditGetRefreshRequest(Request.Method.POST, ACCESS_TOKEN_URI,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, "Token refreshed" + response);
            saveReturn(response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Token failed to refresh = " + error.toString());
          }
        })
    );
  }


  /**
   * checks if the user is currently signed in by checking shared preferences
   * @return whether the user is signed in
   */
  public boolean isAuthenticated() {
    return appContext.getSharedPreferences(preference, Context.MODE_PRIVATE)
        .contains(tokenKey);
  }


  /**
   * parses the successful auth response to store the oauth and refresh token in the shared
   * preferences. Then refreshes the token to get the permanent token
   * @param response
   */
  private void saveReturn(String response) {
    JSONParser parser = new JSONParser();
    try {
      JSONObject data = (JSONObject) parser.parse(response);
      Log.d(TAG, data.get("scope").toString());

      // stores the token in shared preferences
      SharedPreferences.Editor prefEditor = appContext.
          getSharedPreferences("auth", Context.MODE_PRIVATE).edit();

      // checks if there is an refresh token to be stored
      if (data.containsKey(refreshTokenKey)) {
          prefEditor.putString(refreshTokenKey, (String) data.get(refreshTokenKey)).apply();
      }

      prefEditor.putString(tokenKey, (String) data.get(tokenKey)).apply();

      Log.d(TAG, "token saved! " + (String) data.get(tokenKey));
    } catch (ParseException e) {
      Toast.makeText(appContext, "yikes", Toast.LENGTH_SHORT).show();
    }
  }


  class RedditGetTokenRequest extends StringRequest {
    private String code;

    private RedditGetTokenRequest(int method, String url, Response.Listener<String> listener,
                                 Response.ErrorListener errorListener) {
      super(method, url, listener, errorListener);

      code = appContext.getSharedPreferences(preference, Context.MODE_PRIVATE)
          .getString(redirectCode, "DEFAULT");
    }

    // override headers to add custom credentials in client_secret:redirect_code format
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
      // create a new header map and add the right headers to it
      Map<String, String> headers = new HashMap<>();

      // generate encoded credential string with client id and code from redirect
      String credentials = appContext.getString(R.string.client_id) + ":" + code;
      String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
      headers.put("Authorization", auth);

      return headers;
    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError {
      Map<String, String> params = new HashMap<>();

      params.put("grant_type", "authorization_code");
      params.put("code", code);
      params.put("redirect_uri", REDIRECT_URI);
      return params;
    }
  }


  class RedditGetRefreshRequest extends StringRequest{
    private RedditGetRefreshRequest(int method, String url, Response.Listener<String> listener,
                                  Response.ErrorListener errorListener) {
      super(method, url, listener, errorListener);
    }

    // override headers to add custom credentials in client_secret:redirect_code format
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
      // create a new header map and add the right headers to it
      Map<String, String> headers = new HashMap<>();

      String code = appContext.getSharedPreferences(preference, Context.MODE_PRIVATE)
          .getString(redirectCode, "DEFAULT");
      // generate encoded credential string with client id and code from redirect
      String credentials = appContext.getString(R.string.client_id) + ":" + code;
      String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
      headers.put("Authorization", auth);

      return headers;
    }

    public Map<String, String> getParams() throws AuthFailureError {
      Map<String, String> params = new HashMap<>();

      String refreshToken = appContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
          .getString(refreshTokenKey, "DEFAULT");

      params.put("grant_type", refreshTokenKey);
      params.put("refresh_token", refreshToken);

      return params;
    }
  }

}
