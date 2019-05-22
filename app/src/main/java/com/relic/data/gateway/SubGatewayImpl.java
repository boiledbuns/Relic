package com.relic.data.gateway;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.util.Log;

import com.relic.R;
import com.relic.data.ApplicationDB;
import com.relic.network.NetworkRequestManager;
import com.relic.network.request.RelicOAuthRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SubGatewayImpl implements SubGateway {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String NON_OAUTH_ENDPOINT = "https://www.reddit.com/";

  public static String TAG = "SUB_GATEWAY";

  private ApplicationDB appDb;
  public final int GET_SUBINFO = 1;
  public final int SUBSCRIBE = 2;
  public final int UNSUBSCRIBE = 3;

  private NetworkRequestManager requestManager;

  public SubGatewayImpl(Context context, NetworkRequestManager networkRequestManager) {
    appDb = ApplicationDB.getDatabase(context);
    requestManager = networkRequestManager;
  }

  public LiveData<String> getAdditionalSubInfo(String subredditName) {
    MutableLiveData<String> subinfo = new MutableLiveData<>();
    // get sub info
    String end = ENDPOINT + "r/" + subredditName + "/about";
    Log.d(TAG, "from " + end);
    requestManager.processRequest(new RelicOAuthRequest(
            RelicOAuthRequest.GET,
            end,
            response -> {
              JSONParser parser = new JSONParser();
              try {
                Log.d(TAG, response);
                JSONObject subInfoObject = (JSONObject) ((JSONObject) parser.parse(response)).get("data");
                Log.d(TAG, subInfoObject.keySet().toString());

                // user_is_moderator, header_title, subreddit_type, submit_text, display_name, accounts_active, submit_text_html, description_html
                // user_has_favorited, user_is_contributor, user_is_moderator, public_description, active_user_count, user_is_banned
                // public_traffic

                String info = subInfoObject.get("header_title") +
                    "---- accounts active " + subInfoObject.get("active_user_count").toString() +
                    "---- description html " + Html.fromHtml(Html.fromHtml((String) subInfoObject.get("description_html")).toString()) +
                    "---- submit text " + subInfoObject.get("submit_text");
                Log.d(TAG, info);

                // update the subreddit model with the new info
                new InsertSubInfoTask().execute(appDb, subredditName,
                    subInfoObject.get("header_title"),
                    Html.fromHtml(Html.fromHtml((String) subInfoObject.get("description_html")).toString()).toString(),
                    subInfoObject.get("submit_text"));

              } catch (ParseException e) {
                Log.d(TAG, "Error parsing the response");
              }
            },
            error -> Log.d(TAG, "Error retrieving the response from the server")
    ));

    return subinfo;
  }


  private static class InsertSubInfoTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
      ApplicationDB applicationDB = (ApplicationDB) objects[0];

      applicationDB.getSubredditDao().updateSubInfo(
        // subreddit name, headerTitle, description, submitText
          (String) objects[1],
          (String) objects[2],
          (String) objects[3],
          (String) objects[4]
      );
      return null;
    }
  }




  @Override
  public LiveData<String> getSidebar(String subredditName) {
    MutableLiveData<String> sidebar = new MutableLiveData<>();

    // get sub sidebar
    String end = ENDPOINT + "r/" + subredditName + "about/sidebar";
    Log.d(TAG, "from " + end);
    requestManager.processRequest(new RelicOAuthRequest(
            RelicOAuthRequest.GET,
            end,
            response -> {
              Log.d(TAG, "sidebar : " + response);
              sidebar.setValue("test");
            },
            error -> Log.d(TAG, "Error retrieving the response from the server")
    ));

    sidebar.setValue("yeet");
    return sidebar;
  }

  @Override
  public LiveData<Boolean> getIsSubscribed(String subredditName) {
    MutableLiveData<Boolean> isSubscribed = new MutableLiveData<>();
    isSubscribed.setValue(appDb.getSubredditDao().getSubscribed(subredditName) == null);

    return isSubscribed;
  }


  @Override
  public LiveData<Boolean> subscribe(String subName) {
    String end = ENDPOINT + "api/subscribe?action=sub&sr_name=" + subName ;

    Log.d(TAG, "Subscribing to " + end);
    MutableLiveData<Boolean> success = new MutableLiveData<>();

    // delay response request a bit to ensure it doesn't occur until the livedata has been subscribed to
    new Handler().postDelayed(()-> {
        requestManager.processRequest(
            new RelicOAuthRequest(
                RelicOAuthRequest.POST, end,
                response -> {
                  Log.d(TAG, "Subscribed to " + subName);
                  success.setValue(true);
                  // update local entity to reflect the changes once successfully subscribed
                  new UpdateLocalSubSubscription().execute(appDb, subName, true);
                },
                error -> {
                  Log.d(TAG, "Error subscribing to subreddit " + error.networkResponse.headers);
                  success.setValue(false);
                }
            )
        );
      }, 500);

    return success;
  }

  @Override
  public LiveData<Boolean> unsubscribe(String subName) {
    String end = ENDPOINT + "api/subscribe?action=unsub&sr_name=" + subName;

    Log.d(TAG, "Unsubscribing to " + end);
    MutableLiveData<Boolean> success = new MutableLiveData<>();

    new Handler().postDelayed(() -> {
      requestManager.processRequest(new RelicOAuthRequest(
              RelicOAuthRequest.POST,
              end,
              response -> {
                Log.d(TAG, "Unsubscribed to " + subName);
                success.setValue(true);
                // update local entity to reflect the changes once successfully subscribed
                new UpdateLocalSubSubscription().execute(appDb, subName, false);
              },
              error -> {
                Log.d(TAG, "Error unsubscribing to subreddit " + error.networkResponse.headers);
                success.setValue(false);
              }
      ));
    }, 500);

    return success;
  }


  /**
   * Parse response from subreddit into a string
   * @param response JSON representation of the subreddit information
   * @return subreddit information
   */
  private String parseSubredditInfo(String response) {
    String info = response;
    JSONParser parser = new JSONParser();

    try {
      Log.d(TAG, response);
      JSONObject subInfoObject = (JSONObject) ((JSONObject) parser.parse(response)).get("data");
      Log.d(TAG, subInfoObject.keySet().toString());

      // user_is_moderator, header_title, subreddit_type, submit_text, display_name, accounts_active, submit_text_html, description_html
      // user_has_favorited, user_is_contributor, user_is_moderator, public_description, active_user_count, user_is_banned
      // public_traffic

      info = subInfoObject.get("public_description") +
          subInfoObject.get("accounts_active").toString() +
          Html.fromHtml(Html.fromHtml((String) subInfoObject.get("description_html")).toString()) +
          (String) subInfoObject.get("description_html");

    } catch (ParseException e) {
      Log.d(TAG, "Error parsing the response");
      info = "Error parsing the response";
    }

    return info;
  }


  @Override
  public void retrieveSubBanner(String subName) {
    String end = ENDPOINT + "r/" + subName + "/stylesheet.css";
    requestManager.processRequest(new RelicOAuthRequest(
            RelicOAuthRequest.GET, end,
            (String response) -> {
              Log.d(TAG, "subname css : " + response);

              int position = response.indexOf("#header");
              response = response.substring(position);

              // jump to the position of the css property for the banner image
              String backgroundProp = "background-image:url(";
              int bannerUrlPosition = response.indexOf(backgroundProp) + backgroundProp.length() + 1 ;

              // proceed if a background image was found at all
              if (bannerUrlPosition == backgroundProp.length() + 1) {
                Log.d(TAG, " position of banner URL " + bannerUrlPosition);

                boolean complete = false;
                StringBuilder stringBuilder = new StringBuilder();
                // iterate through the response from that position until the full banner image url is parsed
                while (!complete) {
                  char charAtPosition = response.charAt(bannerUrlPosition);
                  // set loop flag to false if the end of the url is found
                  if (charAtPosition == '"') {
                    complete = true;
                  } else {
                    stringBuilder.append(charAtPosition);
                    bannerUrlPosition++;
                  }
                }
                Log.d(TAG, " banner url = " + stringBuilder.toString());
              }
            },
            error -> {
              Log.d(TAG, "Error retrieving response from server " + error.toString());
            }
    ));
  }

  private static class UpdateLocalSubSubscription extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
      ApplicationDB appDB = (ApplicationDB) objects[0];
      String subName = (String) objects[1];
      boolean subStatus = (boolean) objects[2];

      appDB.getSubredditDao().updateSubscription(subStatus, subName);
      return null;
    }
  }

}
