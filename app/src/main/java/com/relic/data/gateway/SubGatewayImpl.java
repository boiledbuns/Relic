package com.relic.data.gateway;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.relic.R;
import com.relic.data.ApplicationDB;
import com.relic.data.Request.RedditOauthRequest;
import com.relic.data.VolleyAccessor;

public class SubGatewayImpl implements SubGateway {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  public static String TAG = "SUB_GATEWAY";
  private String authToken;

  private ApplicationDB appDb;
  public final int GET_SUBINFO = 1;
  public final int SUBSCRIBE = 2;
  public final int UNSUBSCRIBE = 3;

  RequestQueue requestQueue;

  public SubGatewayImpl(Context context) {
    appDb = ApplicationDB.getDatabase(context);
    // Get the key values needed to get the actual authtoken from shared preferences
    String authKey = context.getString(R.string.AUTH_PREF);
    String tokenKey = context.getString(R.string.TOKEN_KEY);

    // retrieve the authtoken for use
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    requestQueue = VolleyAccessor.getInstance(context).getRequestQueue();
  }


  public LiveData<String> getAdditionalSubInfo(String subredditName) {
    MutableLiveData<String> subinfo = new MutableLiveData<>();
    // get sub info and sidebar info
    //route(GET_SUBINFO, subredditName,null, subinfo);

    String end = ENDPOINT + "r/" + subredditName + "/about/rules";
    Log.d(TAG, "from " + end);
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, end,
        (response) -> {
          // determine the appropriate parsing method to be use
          subinfo.setValue(response);
          Log.d(TAG, "Response : " +response);
        },
        (error) -> {
          Log.d(TAG, "Error retrieving the response from the server");
        }, authToken));
    return subinfo;
  }


  @Override
  public LiveData<Boolean> getIsSubscribed(String subredditName) {
    MutableLiveData<Boolean> isSubscribed = new MutableLiveData<>();
    isSubscribed.setValue(appDb.getSubredditDao().getSubscribed(subredditName) == null);

    return isSubscribed;
  }


  /**
   * Routes the api request through this single method based on request
   * @param action
   */
  private void route(int action, String subredditName, String subredditFullname, MutableLiveData<String> livedata) {
    String end = ENDPOINT;
    // determine the appropriate api endpoint to be used
    switch (action) {
      case(GET_SUBINFO): {
        end += "r/" + subredditName + "/sidebar";
      }
    }
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, end,
        (response) -> {
          // determine the appropriate parsing method to be used
          switch (action) {
            case(GET_SUBINFO): {
              livedata.setValue(response);
            }
            case(SUBSCRIBE): {
            }
          }
        },
        (error) -> {
          Log.d(TAG, "Error retrieving the response from the server ");
        }, authToken));
  }


  public void subscribe(String name) {
    String end = ENDPOINT + "api/subscribe?action=sub&sr_name=" + name;
    requestQueue.add(new RedditOauthRequest(Request.Method.POST, end,
        (response -> {
          Log.d(TAG, response);
        }),
        (error -> {
          Log.d(TAG, "Error subscribing to subreddit");
        }), authToken));
  }


  public void unsubscribe(String name) {
    String end = ENDPOINT + "api/subscribe?action=unsub&sr_name=" + name;
    requestQueue.add(new RedditOauthRequest(Request.Method.POST, end,
        (response -> {
          Log.d(TAG, response);
        }),
        (error -> {
          Log.d(TAG, "Error unsubscribing to subreddit");
        }), authToken));
  }
}
