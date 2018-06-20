package com.relic.data;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.relic.R;
import com.relic.data.models.SubredditModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SubRepositoryImpl implements SubRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String KEY = "SUBSCRIBED";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "SUB_REPO";

  private ApplicationDB subDB;
  private Context context;


  public SubRepositoryImpl(Context context) {
    Authenticator auth = new Authenticator(context);
    this.context = context;

    subDB = ApplicationDB.getDatabase(context);
  }


  /**
   * Returns the list of subscribed subs from the database
   * @return list of subscribed subs in the database as livedata
   */
  @Override
  public LiveData<List<SubredditModel>> getSubscribedSubs() {
    return subDB.getSubredditDao().getAllSubscribed();
  }


  @Override
  public void retrieveMoreSubscribedSubs(String after) {
    String ending = "";
    // change the string if not refreshing the entire list of subscribed subreddits
    if (after != null) {
      ending = "?after=" + after + "&limit=50";
    }

    // create the new request to reddit servers and store the data in persistence layer
    VolleyQueue.getQueue().add(
        new StringRequest(
            Request.Method.GET, ENDPOINT + "subreddits/mine/subscriber" + ending,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                try {
                  parseSubreddits(response);
                } catch (ParseException e) {
                  Log.e(TAG, "Error parsing the response: " + e.toString());
                }
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error : " + error.getMessage());
              }
            })
        {
          public Map<String, String> getHeaders() {
            Map <String, String> headers = new HashMap<>();

            String auth = context.getSharedPreferences(
                context.getResources().getString(R.string.AUTH_PREF),
                Context.MODE_PRIVATE)
                .getString(context.getResources().getString(R.string.TOKEN_KEY), "DEFAULT");

            // generate the credential string for oauth
            String credentials = "bearer " + auth;
            headers.put("Authorization", credentials);
            headers.put("User-Agent", userAgent);

            return headers;
          }
        });
  }



  private void parseSubreddits(String response) throws ParseException {
    //Log.d(TAG, response);
    JSONObject data = (JSONObject) ((JSONObject) new JSONParser().parse(response)).get("data");
    List <SubredditModel> subscribed = new ArrayList<>();
    String after = (String) data.get("after");

    // get all the subs that the user is subscribed to
    JSONArray subs = (JSONArray) data.get("children");
    Iterator subIterator = subs.iterator();

    while (subIterator.hasNext()) {
      JSONObject currentSub = (JSONObject) ((JSONObject) subIterator.next()).get("data");
      boolean nsfw = true;
      if (currentSub.get("nsfw") == null) {
        nsfw = false;
      }
      //Log.d(TAG, "keys = " + currentSub.keySet());
      subscribed.add(new SubredditModel(
          (String) currentSub.get("id"),
          (String) currentSub.get("display_name"),
          (String) currentSub.get("banner_img"),
          nsfw
      ));
    }

    Log.d(TAG, "retrieved = " + subscribed.size() + " " + after);
    //Log.d(TAG, subscribed.toString());
    // insert the subs and listing into the room instance
    new InsertSubsTask(this, subDB, subscribed).execute(after);
  }


  static class InsertSubsTask extends AsyncTask <String, Integer, Integer> {
    private ApplicationDB subDB;
    private SubRepository subRepo;
    private List<SubredditModel> subs;
    private String after;

    InsertSubsTask(SubRepository subRepo, ApplicationDB subDB, List<SubredditModel> subs) {
      this.subDB = subDB;
      this.subRepo = subRepo;
      this.subs = subs;
    }

    @Override
    protected Integer doInBackground(String... Strings) {
      subDB.getSubredditDao().insertAll(subs);
      // stores the after value to be used to retrieve the next listing
      after = Strings[0];
      return subs.size();
    }

    @Override
    protected void onPostExecute(Integer integer) {
      super.onPostExecute(integer);
      if (after != null) {
        // retrieve more subs without refreshing if the string is null
        subRepo.retrieveMoreSubscribedSubs(after);
      }
    }
  }


}
