package com.relic.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.relic.R;
import com.relic.data.Request.RedditOauthRequest;
import com.relic.data.entities.ListingEntity;
import com.relic.data.entities.SubredditEntity;
import com.relic.data.gateway.SubGateway;
import com.relic.data.gateway.SubGatewayImpl;
import com.relic.data.models.SubredditModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SubRepositoryImpl implements SubRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String KEY = "SUBSCRIBED";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "SUB_REPO";

  private ApplicationDB appDb;
  private Context context;
  private RequestQueue volleyQueue;
  private String authToken;
  private JSONParser parser;
  private Gson gson;

  MutableLiveData<List<String>> searchResults;

  public SubRepositoryImpl(Context context) {
    Authenticator auth = new Authenticator(context);
    this.context = context;
    volleyQueue = VolleyAccessor.getInstance(context).getRequestQueue();
    parser = new JSONParser();
    gson = new GsonBuilder().create();

    // retrieve the auth token shared preferences
    String authKey = context.getResources().getString(R.string.AUTH_PREF);
    String tokenKey = context.getResources().getString(R.string.TOKEN_KEY);
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    appDb = ApplicationDB.getDatabase(context);
  }


  /**
   * Returns the list of subscribed subs from the database
   * @return list of subscribed subs in the database as livedata
   */
  @Override
  public LiveData<List<SubredditModel>> getSubscribedSubs() {
    return appDb.getSubredditDao().getAllSubscribed();
  }


  @Override
  public void retrieveMoreSubscribedSubs(String after) {
    String ending = "";
    // change the string if not refreshing the entire list of subscribed subreddits
    if (after != null) {
      ending = "?limit=50&after=" + after;
    }

    // create the new request to reddit servers and store the data in persistence layer
    volleyQueue.add(new RedditOauthRequest(
        Request.Method.GET, ENDPOINT + "subreddits/mine/subscriber" + ending,
        response -> {
          try {
            List<SubredditEntity> subreddts = parseSubreddits(response);
            // insert the subs and listing into the room instance
            new InsertSubsTask(this, appDb, parseAfterValue(response),
                parseSubreddits(response), after == null).execute(parseAfterValue(response).afterPosting);

          } catch (ParseException e) {
            Log.e(TAG, "Error parsing the response: " + e.toString());
          }
        },
        error -> Log.d(TAG, "Error : " + error.getMessage()), authToken));
  }


  private ListingEntity parseAfterValue(String response) throws ParseException {
    JSONObject data = (JSONObject) ((JSONObject) parser.parse(response)).get("data");
    // create a new listing to ensure that the db has an "after" value for checking if we need to
    // fetch more values or not
    return new ListingEntity(TAG, (String) data.get("after"));
  }


  private List<SubredditEntity> parseSubreddits(String response) throws ParseException {
    //Log.d(TAG, response);
    // GSON reader used to unmarshall json objects

    JSONObject data = (JSONObject) ((JSONObject) parser.parse(response)).get("data");
    List <SubredditEntity> subscribed = new ArrayList<>();

    // get all the subs that the user is subscribed to
    JSONArray subs = (JSONArray) data.get("children");
    Iterator subIterator = subs.iterator();

    while (subIterator.hasNext()) {
      JSONObject currentSub = (JSONObject) ((JSONObject) subIterator.next()).get("data");
      Log.d(TAG, "keys = " + currentSub.keySet());
      subscribed.add(gson.fromJson(currentSub.toJSONString(), SubredditEntity.class));
    }

    //Log.d(TAG, subscribed.toString());
    return subscribed;
  }


  public LiveData<SubredditModel> getSingleSub(String subName) {
    return appDb.getSubredditDao().getSub(subName);
  }

  @Override
  public void retrieveSingleSub(String subName) {
    volleyQueue.add(new RedditOauthRequest(Request.Method.GET, ENDPOINT + "r/" + subName  +"/about",
        (String response) -> {
          Log.d(TAG, response);

          try {
            // parse the response and add it to an arraylist to be inserted in the db
            JSONObject subredditObject = (JSONObject) ((JSONObject) parser.parse(response)).get("data");
            SubredditEntity subreddit = gson.fromJson(subredditObject.toJSONString(), SubredditEntity.class);

            // create a new task to insert the subreddits on parse success
            new InsertSubTask().execute(appDb, subreddit);

          } catch (ParseException e) {
            e.printStackTrace();
          }

        }, (VolleyError e) -> {
      Log.d(TAG, "There was an error retrieving the respone from the server " + e.getMessage());
    }, authToken));
  }


  static class InsertSubsTask extends AsyncTask <String, Integer, Integer> {
    private ApplicationDB subDB;
    private SubRepository subRepo;
    private List<SubredditEntity> subs;
    private String after;
    private ListingEntity listing;
    private boolean delete;

    InsertSubsTask(SubRepository subRepo, ApplicationDB subDB, ListingEntity listing, List<SubredditEntity> subs, boolean delete) {
      this.subDB = subDB;
      this.subRepo = subRepo;
      this.subs = subs;
      this.listing = listing;
      this.delete = delete;
    }

    @Override
    protected Integer doInBackground(String... Strings) {
      if (delete) {
        subDB.getSubredditDao().deleteAll();
      }
      subDB.getSubredditDao().insertAll(subs);
      // stores the after value to be used to retrieve the next listing
      after = Strings[0];
      // update the listing value if it isn't null (not refresh)
      if (after != null) {
        subDB.getListingDAO().insertListing(listing);
      }
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


  @Override
  public LiveData<List<SubredditModel>> findSubreddit(String subredditName) {
    return null;
  }


  @Override
  public LiveData<List<String>> searchSubreddits(String searchValue) {
    if (searchResults == null) {
      searchResults= new MutableLiveData<>();
    }

    String end = ENDPOINT + "api/search_subreddits?query=" + searchValue;
      volleyQueue.add(new RedditOauthRequest(Request.Method.POST, end,
          response -> {
            Log.d(TAG, response);
            // parse the reponse as a list of sub names and update the livedata accordingly
            searchResults.setValue(parseSearchedSubs(response));
          },
          error -> {
            Log.d(TAG, "error retrieving this search results");
          }, authToken));

    return searchResults;
  }


  /**
   * Parses the api response to obtain the list of subreddit names
   * @param response search_subreddits api response
   */
  private List<String> parseSearchedSubs(String response) {
    List <String> parsedMatches = new ArrayList<>();

    try {
      JSONArray subreddits = (JSONArray) ((JSONObject) parser.parse(response)).get("subreddits");
      Iterator subIterator = subreddits.iterator();

      while (subIterator.hasNext()) {
        parsedMatches.add((String) ((JSONObject) subIterator.next()).get("name"));
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return  parsedMatches;
  }


  @Override
  public SubGateway getSubGateway() {
    return new SubGatewayImpl(context);
  }


  static class InsertSubTask extends AsyncTask <Object, Integer, Integer> {
    @Override
    protected Integer doInBackground(Object... objects) {
      ApplicationDB applicationDB = (ApplicationDB) objects[0];
      applicationDB.getSubredditDao().insert((SubredditEntity) objects[1]);
      return null;
    }
  }

}
