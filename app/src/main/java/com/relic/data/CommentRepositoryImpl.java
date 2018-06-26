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
import com.relic.data.entities.CommentEntity;
import com.relic.data.entities.ListingEntity;
import com.relic.data.models.CommentModel;
import com.relic.domain.Listing;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommentRepositoryImpl implements CommentRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "COMMENT_REPO";

  private ApplicationDB appDB;
  private Context viewContext;
  private RequestQueue queue;
  private JSONParser JSONParser;

  String authToken;

  public CommentRepositoryImpl (Context context) {
    //TODO convert VolleyQueue into a singleton
    appDB = ApplicationDB.getDatabase(context);
    queue = Volley.newRequestQueue(context);
    JSONParser = new JSONParser();

    // TODO convert this to a authenticator method
    // retrieve the auth token shared preferences
    String authKey = context.getResources().getString(R.string.AUTH_PREF);
    String tokenKey = context.getResources().getString(R.string.TOKEN_KEY);
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    viewContext = context;
  }


  /**
   * Exposes the list of comments as livedata
   * @param postFullname fullname of the post to retrieve comments for
   * @return list of comments as livedata
   */
  @Override
  public LiveData<List<CommentModel>> getComments(String postFullname) {
    return appDB.getCommentDAO().getComments(postFullname);
  }



  @Override
  public void retrieveComments(String subName, String postFullName, String after) {
    String ending = "r/" + subName + "/comments/" + postFullName.substring(3) + "?count=20";
    Log.d(TAG, ENDPOINT + ending);
    if (after != null) {
      ending += "&after=" + after;
    }
    queue.add(new RedditOauthRequest(Request.Method.GET, ENDPOINT + ending,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, response);
            try {
              parseComments(postFullName, response);
            } catch (Exception e) {
              Log.d(TAG, "Error parsing JSON return " + e.getMessage());
            }
          }
        }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Error with request : " +  error.getMessage());
          }
        }));
    }


  class RedditOauthRequest extends StringRequest {
    private RedditOauthRequest(int method, String url, Response.Listener<String> listener,
                               Response.ErrorListener errorListener) {
      super(method, url, listener, errorListener);
    }

    public Map<String, String> getHeaders() {
      Map <String, String> headers = new HashMap<>();

      // generate the credential string for oauth
      String credentials = "bearer " + authToken;
      headers.put("Authorization", credentials);
      headers.put("User-Agent", userAgent);

      return headers;
    }
  }


  /**
   * Parse the response from the api and store the comments in the room db
   * @param response json string response
   * @param postFullName fullname of post used as a key for the "after" value
   * @throws ParseException potential issue with parsing of json structure
   */
  private void parseComments(String postFullName, String response) throws ParseException {
    Gson gson = new GsonBuilder().create();

    JSONArray array = (JSONArray) JSONParser.parse(response);
    JSONObject comments = (JSONObject) array.get(1);
    Log.d(TAG, comments.keySet().toString());

    // get the data from the listing object
    comments = (JSONObject) comments.get("data");
    ListingEntity listing = new ListingEntity(postFullName, (String) comments.get("after"));
    Log.d(TAG, "after " + (String) comments.keySet().toString() + " " + (String) comments.get("after"));

    // get the list of children (comments) associated with the post
    JSONArray commentChildren = ((JSONArray) comments.get("children"));

    Iterator commentIterator = commentChildren.iterator();
    List<CommentEntity> commentEntities = new ArrayList<>();

    JSONObject commentPOJO;
    while (commentIterator.hasNext()) {
      commentPOJO = (JSONObject) ((JSONObject) commentIterator.next()).get("data");

      // unmarshall the comment pojo and add it to list
      commentEntities.add(gson.fromJson(commentPOJO.toString(), CommentEntity.class));
      Log.d(TAG, commentPOJO.get("id").toString());
    }

    // insert comments
    new InsertCommentsTask(appDB, commentEntities, listing).execute();
  }


  private static class InsertCommentsTask extends AsyncTask<String, Integer, Integer> {
    ApplicationDB db;
    List<CommentEntity> comments;
    ListingEntity listing;

    InsertCommentsTask(ApplicationDB appDB, List<CommentEntity> commentEntities, ListingEntity listing) {
      this.db = appDB;
      this.comments = commentEntities;
      this.listing = listing;
    }

    @Override
    protected Integer doInBackground(String... strings) {
      db.getCommentDAO().insertComments(comments);
      db.getListingDAO().insertListing(listing);
      return null;
    }
  }


}
