package com.relic.data.gateway;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.relic.R;
import com.relic.network.NetworkRequestManager;
import com.relic.network.request.RelicOAuthRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserGatewayImpl implements UserGateway{
  private final String ENDPOINT = "https://oauth.reddit.com/";
  public static String TAG = "USER_GATEWAY";
  private String authToken;

  private NetworkRequestManager requestManager;

  public UserGatewayImpl(Context context, NetworkRequestManager networkRequestManager) {
    // Get the key values needed to get the actual authtoken from shared preferences
    String authKey = context.getString(R.string.AUTH_PREF);
    String tokenKey = context.getString(R.string.TOKEN_KEY);

    // retrieve the authtoken for use
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    requestManager = networkRequestManager;
  }

  public void getUser(String username) {
    String endpoint = ENDPOINT + "/user/" + username + "/about";
    requestManager.processRequest(new RelicOAuthRequest(
            RelicOAuthRequest.GET,
            endpoint,
            response ->  {
              parseUser(response);
            },
            error -> {
                Log.d(TAG, "Error getting user overview");
            },
            authToken
    ));
  }

  private void parseUser(String response) {
    Log.d(TAG, response);
    JSONParser parser = new JSONParser();

    try {
      JSONObject full = (JSONObject) ((JSONObject) parser.parse(response)).get("data");

      Log.d(TAG, full.keySet().toString());
    }
    catch (ParseException e) {
       Log.d(TAG, "Error parsing the response");
    }
  }


  private static class another extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
      return null;
    }
  }


}
