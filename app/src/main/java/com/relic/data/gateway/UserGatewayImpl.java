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

public class UserGatewayImpl implements UserGateway {
    public static String TAG = "USER_GATEWAY";
    private final String ENDPOINT = "https://oauth.reddit.com/";

    private NetworkRequestManager requestManager;

    public UserGatewayImpl(Context context, NetworkRequestManager networkRequestManager) {
        requestManager = networkRequestManager;
    }

    public void getUser(String username) {
        String endpoint = ENDPOINT + "/user/" + username + "/about";
        requestManager.processRequest(new RelicOAuthRequest(
            RelicOAuthRequest.GET,
            endpoint,
            response -> {
                parseUser(response);
            },
            error -> {
                Log.d(TAG, "Error getting user overview");
            }
        ));
    }

    private void parseUser(String response) {
        Log.d(TAG, response);
        JSONParser parser = new JSONParser();

        try {
            JSONObject full = (JSONObject) ((JSONObject) parser.parse(response)).get("data");

            Log.d(TAG, full.keySet().toString());
        } catch (ParseException e) {
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
