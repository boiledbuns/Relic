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
import com.relic.data.entities.CommentEntity;
import com.relic.data.entities.ListingEntity;
import com.relic.data.models.CommentModel;
import com.relic.domain.Listing;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommentRepositoryImpl implements CommentRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "COMMENT_REPO";

  private ApplicationDB appDB;
  private Context viewContext;
  private RequestQueue queue;
  private JSONParser JSONParser;

  private String authToken;

  public CommentRepositoryImpl (Context context) {
    //TODO convert VolleyQueue into a singleton
    appDB = ApplicationDB.getDatabase(context);
    queue = VolleyAccessor.getInstance(context).getRequestQueue();
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
        }, authToken));
    }


  @Override
  public void clearComments(String postFullname) {
    new ClearCommentsTask().execute(appDB, postFullname);
  }

  private static class ClearCommentsTask extends AsyncTask<Object, Integer, Integer> {
    @Override
    protected Integer doInBackground(Object... objects) {
      ApplicationDB appDB = (ApplicationDB) objects[0];
      String postFullname = (String) objects[1];

      // delete the locally stored post comment data using the comment dao
      appDB.getCommentDAO().deletePostComments(postFullname);
      return null;
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
    Log.d(TAG, "after " + comments.keySet().toString() + " " + comments.get("after"));

    // get the list of children (comments) associated with the post
    JSONArray commentChildren = ((JSONArray) comments.get("children"));

    Iterator commentIterator = commentChildren.iterator();
    List<CommentEntity> commentEntities = new ArrayList<>();

    // initialize the date formatter and date for now
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd',' hh:mm a");
    Date current = new Date();

    JSONObject commentPOJO;
    while (commentIterator.hasNext()) {
      commentPOJO = (JSONObject) ((JSONObject) commentIterator.next()).get("data");

      CommentEntity commentEntity = gson.fromJson(commentPOJO.toString(), CommentEntity.class);
      Log.d(TAG, "comments keys : " + commentPOJO.keySet());
      Log.d(TAG, "replies : " + commentPOJO.get("collapsed") + " " + commentPOJO.get("keys"));

      Boolean likes = (Boolean) commentPOJO.get("likes");
      commentEntity.userUpvoted = likes == null ? 0 : (likes ? 1 : -1);
      commentEntity.setId((String) commentPOJO.get("id"));

      // add year to stamp if the post year doesn't match the current one
      Date created = new Date((long) ((double) commentPOJO.get("created"))*1000);
      if (current.getYear() != created.getYear()) {
        commentEntity.created = created.getYear() + " " + formatter.format(created);
      } else {
        commentEntity.created = formatter.format(created);
      }

      // unmarshall the comment pojo and add it to list
      commentEntities.add(commentEntity);
      Log.d(TAG, commentPOJO.get("id").toString());
    }

    // insert the comments
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
