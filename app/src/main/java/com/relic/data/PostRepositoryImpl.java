package com.relic.data;


import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.relic.R;
import com.relic.data.Request.RedditOauthRequest;
import com.relic.data.dao.PostDao;
import com.relic.data.models.SubredditModel;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.data.entities.ListingEntity;
import com.relic.data.entities.PostEntity;
import com.relic.data.models.PostModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PostRepositoryImpl implements PostRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "POST_REPO";

  private Context context;
  private String authToken;
  private JSONParser JSONParser;
  private ApplicationDB appDB;

  private RequestQueue requestQueue;


  public PostRepositoryImpl(Context context) {
    this.context = context;
    requestQueue = VolleyAccessor.getInstance(context).getRequestQueue();
    JSONParser = new JSONParser();

    // get the oauth token from the app's shared preferences
    String authKey = context.getResources().getString(R.string.AUTH_PREF);
    String tokenKey = context.getResources().getString(R.string.TOKEN_KEY);
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    // initialize reference to the database
    appDB = ApplicationDB.getDatabase(context);
  }


  /**
   * Exposes the livedata list of posts
   * @param subreddit subreddit to get the list for
   * @return livedata list of posts
   */
  public LiveData<List<PostModel>> getPosts(String subreddit) {
    return appDB.getPostDao().getSubredditPosts(subreddit);
  }


//  public void retrieveMorePosts2(String subredditName, String after) {
//
//  }
//
//
//  public void reset(){
//
//  }



  /**
   * Retrieves posts for a subreddit
   * @param subredditName name of the subreddit
   * @param after the full name of the page to retrieve the posts from
   */
  @Override
  public void retrieveMorePosts(String subredditName, String after) {
    String ending = "r/" + subredditName;
    // reset posts for this subreddit if the after values have been set to null
    if (after == null) {
      // delete all the current items for the db
      new DeleteSubPostsTask(appDB).execute(subredditName);
    }
    else {
      // change the api endpoint to access to get the next post listing
      ending += "?after=" + after;
    }

    // create the new request and submit it
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, ENDPOINT + ending,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            //Log.d(TAG, "Loaded reponse ");
            try {
              parsePosts(response, subredditName);
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
    }, authToken));
  }


  /**
   * Parses the response from the api and stores the posts in the persistence layer
   * @param response the json response from the server with the listing object
   * @throws ParseException
   */
  private void parsePosts(String response, String subreddit) throws ParseException {
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
      Log.d(TAG, "src : " + post.get("src") + ", media domain url = "+ post.get("media_domain_url"));
      Log.d(TAG, "media embed : " + post.get("media_embed") + ", media = "+ post.get("media"));
      Log.d(TAG, "preview : " + post.get("preview") + " "+ post.get("url"));
      //Log.d(TAG, "post keys " + post.keySet().toString());

      // unmarshall the object and add it into a list
      postEntities.add(gson.fromJson(post.toJSONString(), PostEntity.class));
    }

    // insert all the post entities into the db
    new InsertPostsTask(appDB, postEntities).execute(listing);
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
            Log.d(TAG, "Error: " + error.networkResponse.statusCode);
          }
        }, authToken));
  }


  private PostEntity parsePost(String response) throws ParseException {
    Gson gson = new GsonBuilder().create();
    JSONObject data = (JSONObject)((JSONObject) ((JSONArray) JSONParser.parse(response)).get(0)).get("data");
    JSONObject child = (JSONObject) ((JSONArray) data.get("children")).get(0);
    JSONObject post = (JSONObject) child.get("data");

    return gson.fromJson(post.toJSONString(), PostEntity.class);
  }

  private class InsertPostTask extends AsyncTask <Object, Integer, Integer> {
    @Override
    protected Integer doInBackground(Object... objects) {
      ApplicationDB applicationDB = (ApplicationDB) objects[0];
      applicationDB.getPostDao().insertPost((PostEntity) objects[1]);
      return null;
    }
  }

}
