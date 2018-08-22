package com.relic.data;


import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.relic.R;
import com.relic.data.Request.RedditOauthRequest;
import com.relic.data.gateway.PostGateway;
import com.relic.data.gateway.PostGatewayImpl;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.data.entities.ListingEntity;
import com.relic.data.entities.PostEntity;
import com.relic.data.models.PostModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostRepositoryImpl implements PostRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "POST_REPO";

  private String[] sortByMethods = {"best", "controversial", "hot", "new", "rising", "top"};
  private String[] sortScopes = {"hour", "day", "week", "month", "year", "all"};

  private Context currentContext;
  private JSONParser JSONParser;
  private ApplicationDB appDB;

  private RequestQueue requestQueue;
  private int currentSortingCode = PostRepository.SORT_HOT;


  public PostRepositoryImpl(Context context) {
    currentContext = context;
    requestQueue = VolleyAccessor.getInstance(context).getRequestQueue();
    JSONParser = new JSONParser();

    // initialize reference to the database
    appDB = ApplicationDB.getDatabase(context);
  }

  // get the oauth token from the app's shared preferences
  private String checkToken() {
    // retrieve the auth token shared preferences
    String authKey = currentContext.getResources().getString(R.string.AUTH_PREF);
    String tokenKey = currentContext.getResources().getString(R.string.TOKEN_KEY);
    return currentContext.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");
  }

  /**
   * Exposes the livedata list of posts
   * @param subreddit subreddit to get the list for
   * @return livedata list of posts
   */
  public LiveData<List<PostModel>> getPosts(String subreddit) {
    return appDB.getPostDao().getSubredditPosts(subreddit);
  }


  /**
   * Retrieves posts for a subreddit
   * @param subredditName name of the subreddit
   * @param after the full name of the page to retrieve the posts from
   */
  @Override
  public void retrieveMorePosts(String subredditName, @NonNull String after) {
    // change the api endpoint to access to get the next post listing
    String ending = "r/" + subredditName + "?after=" + after;

    // create the new request and submit it
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, ENDPOINT + ending,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            //Log.d(TAG, "Loaded response ");
            try {
              parsePosts(response, subredditName);
              //new InsertPostsTask(appDB, parsePosts(response, subredditName)).execute();
            }
            catch (ParseException error) {
              Log.d(TAG, "Error: " + error.getMessage());
            }
          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "Error: " + error.getMessage());
      }
    }, checkToken()));
  }



  public void retrieveSortedPosts(String subredditName, int sortByCode) {
    retrieveSortedPosts(subredditName, sortByCode, 0);
  }


  /**
   * Deletes all locally stored posts and retrieves a new set based on the sorting method specified
   * by the caller
   * @param subredditName name of the subreddit for the posts to be retrieved
   * @param sortByCode code for the associated sort by method
   * @param sortScopeCode  code for the associate time span to sort by
   */
  @Override
  public void retrieveSortedPosts(String subredditName, int sortByCode, int sortScopeCode) {
    // generate the ending of the request url based on sorting mzethod specified by the used
    String ending = ENDPOINT + "r/" + subredditName;

    // change the endpoint based on which sorting option the user has selected
    if (sortByCode != SORT_DEFAULT && sortByCode <= sortByMethods.length) {
      // build the appropriate endpoint based on the "sort by" code and time scope
      ending += "/" + sortByMethods[sortByCode - 1];

      // add the scope to the query string if it has been selected
      if (sortScopeCode != PostRepository.SCOPE_NONE) {
        ending += "?t=" + sortScopes[sortScopeCode - 1];
      }
    }

    Log.d(TAG, ending);
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, ending,
        (String response) -> {
          Log.d(TAG, response);
          try {
            parsePosts(response, subredditName);
          } catch (ParseException e) {
            e.printStackTrace();
          }

        },
        (VolleyError volleyError) -> {
          Log.d(TAG, "Error retrieving sorted posts " + volleyError.networkResponse);
        }, checkToken()));
  }


  /**
   * Parses the response from the api and stores the posts in the persistence layer
   * @param response the json response from the server with the listing object
   * @throws ParseException
   */
  private List<PostEntity> parsePosts(String response, String subreddit) throws ParseException {
    //TODO separate into two separate methods and switch to multithreaded to avoid locking main thread
    JSONObject listingData = (JSONObject) ((JSONObject) JSONParser.parse(response)).get("data");
    JSONArray listingPosts = (JSONArray) (listingData).get("children");

    // create the new listing entity
    ListingEntity listing = new ListingEntity(subreddit, (String) listingData.get("after"));
    Log.d(TAG, "Listing after val : " + listing.afterPosting);

    // GSON reader to unmarshall the json response
    Gson gson = new GsonBuilder().create();

    Iterator postIterator = listingPosts.iterator();
    List <PostEntity> postEntities = new ArrayList<>();

    // generate the list of posts using the json array
    while (postIterator.hasNext()) {
      JSONObject post = (JSONObject) ((JSONObject) postIterator.next()).get("data");
      //Log.d(TAG, "post : " + post.get("title") + " "+ post.get("author"));
      //Log.d(TAG, "src : " + post.get("src") + ", media domain url = "+ post.get("media_domain_url"));
      //Log.d(TAG, "media embed : " + post.get("media_embed") + ", media = "+ post.get("media"));
      //Log.d(TAG, "preview : " + post.get("preview") + " "+ post.get("url"));
      Log.d(TAG, "link_flair_richtext : " + post.get("link_flair_richtext") + " "+ post.get("author_flair_text"));
      Log.d(TAG, "post keys " + post.keySet().toString());

      // unmarshall the object and add it into a list
      postEntities.add(gson.fromJson(post.toJSONString(), PostEntity.class));
    }

    // insert all the post entities into the db
    new InsertPostsTask(appDB, postEntities).execute(listing);

    return postEntities;
  }


  /**
   * Async task to insert posts and create/update the listing data for the current subreddit to
   * point to the next listing
   */
  static class InsertPostsTask extends AsyncTask<ListingEntity, Integer, Integer> {
    ApplicationDB appDB;
    List<PostEntity> postList;

    InsertPostsTask(ApplicationDB db, List<PostEntity> posts) {
      super();
      appDB = db;
      postList = posts;
    }

    @Override
    protected Integer doInBackground(ListingEntity... listing) {
      appDB.getPostDao().insertPosts(postList);
      appDB.getListingDAO().insertListing(listing[0]);
      return null;
    }
  }

  /**
   * Retrieves the "after" values to be used for the next post listing
   * @param callback callback to send the name to
   * @param subName name of the sub to retrieve the "after" value for
   */
  public void getNextPostingVal(RetrieveNextListingCallback callback, String subName) {
    new RetrieveListingAfterTask(appDB, callback).execute(subName);
  }


  static class RetrieveListingAfterTask extends AsyncTask<String, Integer, Integer> {
    ApplicationDB appDB;
    RetrieveNextListingCallback callback;

    RetrieveListingAfterTask(ApplicationDB appDB, RetrieveNextListingCallback callback) {
      super();
      this.appDB = appDB;
      this.callback = callback;
    }

    @Override
    protected Integer doInBackground(String... strings) {
      // get the "after" value for the most current sub listing
      String subAfter = appDB.getListingDAO().getNext(strings[0]);
      callback.onNextListing(subAfter);
      return null;
    }
  }


  static class DeleteSubPostsTask extends AsyncTask<String, Integer, Integer> {
    ApplicationDB appDB;
    RetrieveNextListingCallback callback;

    DeleteSubPostsTask(ApplicationDB appDB) {
      super();
      this.appDB = appDB;
    }

    @Override
    protected Integer doInBackground(String... strings) {
      // delete the posts associated with the current sub
      appDB.getPostDao().deleteAllFromSub(strings[0]);
      return null;
    }
  }

  
  public LiveData<PostModel> getPost(String postFullName) {
    return appDB.getPostDao().getSinglePost(postFullName);
  }


  @Override
  public void retrievePost(String subredditName, String postFullName) {
    String ending = ENDPOINT + "r/" + subredditName + "/comments/" + postFullName.substring(3);

    Log.d(TAG, ending);

    // create the new request and submit it
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, ending,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                Log.d(TAG, "Loaded response " + response);
                try {
                  PostEntity post = parsePost(response);
                  new InsertPostTask().execute(appDB, post);
                }
                catch (ParseException error) {
                  Log.d(TAG, "Error: " + error.getMessage());
                }
              }
            }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Error: "   + error.networkResponse.statusCode);
          }
        }, checkToken()));
  }


  private PostEntity parsePost(String response) throws ParseException {
    Gson gson = new GsonBuilder().create();
    JSONObject data = (JSONObject)((JSONObject) ((JSONArray) JSONParser.parse(response)).get(0)).get("data");
    JSONObject child = (JSONObject) ((JSONArray) data.get("children")).get(0);
    JSONObject post = (JSONObject) child.get("data");

    return gson.fromJson(post.toJSONString(), PostEntity.class);
  }

  private static class InsertPostTask extends AsyncTask <Object, Integer, Integer> {
    @Override
    protected Integer doInBackground(Object... objects) {
      ApplicationDB applicationDB = (ApplicationDB) objects[0];
      applicationDB.getPostDao().insertPost((PostEntity) objects[1]);
      return null;
    }
  }

  @Override
  public void clearAllSubPosts(String subredditName) {
    new ClearSubredditPosts().execute(appDB, subredditName);
  }

  private static class ClearSubredditPosts extends AsyncTask <Object, Integer, Integer>{
    @Override
      protected Integer doInBackground(Object... objects) {
        ApplicationDB appDB = (ApplicationDB) objects[0];
        appDB.getPostDao().deleteAllFromSub((String) objects[1]);

        return null;
      }
  }

  @Override
  public PostGateway getPostGateway() {
    return new PostGatewayImpl(currentContext);
  }
}
