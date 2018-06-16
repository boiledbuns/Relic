package com.relic.data;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.relic.R;
import com.relic.data.models.CommentModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommentRepositoryImpl implements CommentRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "COMMENT_REPO";

  private Context viewContext;
  private RequestQueue queue;
  private JSONParser JSONParser;

  String authToken;

  public CommentRepositoryImpl (Context context) {
    //TODO convert VolleyQueue into a singleton
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


    return null;
  }



  @Override
  public void retrieveComments(String subName, String postFullName, String after) {
    String ending = "r/" + subName + "/comments/" + postFullName.substring(3);
    Log.d(TAG, ENDPOINT + ending);
    if (after != null) {
      //ending += after;
    }
    queue.add(new RedditOauthRequest(Request.Method.GET, ENDPOINT + ending,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, response);
            try {
              parseComments(response);
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


  private void parseComments(String response) throws ParseException {
    JSONArray array = (JSONArray) JSONParser.parse(response);
    JSONObject comments = (JSONObject) array.get(1);
    Log.d(TAG, comments.keySet().toString());

    comments = (JSONObject) comments.get("data");
    Log.d(TAG, comments.keySet().toString());

    array = ((JSONArray) comments.get("children"));
    Iterator arrayIterator = array.iterator();

    while (arrayIterator.hasNext()) {
      comments = (JSONObject) arrayIterator.next();
      comments = (JSONObject) comments.get("data");

      Log.d(TAG, comments.get("body_html").toString());

    }
    Log.d(TAG, "END");
  }



}
