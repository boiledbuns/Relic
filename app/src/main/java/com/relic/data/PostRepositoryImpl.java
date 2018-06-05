package com.relic.data;


import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.relic.R;
import com.relic.data.models.PostListing;
import com.relic.domain.Post;
import com.relic.domain.post.PostImpl;

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

  RequestQueue requestQueue;

  public PostRepositoryImpl(Context context) {
    this.context = context;
    requestQueue = Volley.newRequestQueue(context);
    JSONParser = new JSONParser();

    // get the oauth token from the app's shared preferences
    String authKey = context.getResources().getString(R.string.AUTH_PREF);
    String tokenKey = context.getResources().getString(R.string.TOKEN_KEY);
    authToken = context.getSharedPreferences(authKey, Context.MODE_PRIVATE)
        .getString(tokenKey, "DEFAULT");

    Toast.makeText(context, "AUTH TOKEN = " + authToken, Toast.LENGTH_SHORT).show();
  }


  public void getPosts(String subreddit) {
    // create the new request and submit it
    requestQueue.add(new RedditOauthRequest(Request.Method.GET, ENDPOINT + subreddit,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            //Log.d(TAG, response);
            try {
              parsePosts(response);
            }
            catch (ParseException error) {
              Log.d(TAG, "Error: " + error.getMessage());
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Error: " + error.networkResponse.headers.toString());
          }
        }
    ));
  }

  @Override
  public LiveData<PostListing> getPostListing(String subredditName) {
    return null;
  }

  @Override
  public void retrieveNextPostListing(String listingAfter) {

  }


  /**
   * Parses the response from the api and stores the data in the persistence layer
   * @param response the json response from the server with the listing object
   * @throws ParseException
   */
  private void parsePosts(String response) throws ParseException {
    JSONObject listingData = (JSONObject) ((JSONObject) JSONParser.parse(response)).get("data");
    JSONArray listingPosts = (JSONArray) (listingData).get("children");

    Iterator postIterator = listingPosts.iterator();
    List <Post> posts = new ArrayList<>();
    Log.d(TAG, "dank = " + response);

    // generate the list of posts using the json array
    while (postIterator.hasNext()) {
      JSONObject post = (JSONObject) ((JSONObject) postIterator.next()).get("data");
      posts.add(new PostImpl(
          (String) post.get("id"),
          (String) post.get("subreddit_name_prefixed"),
          (String) post.get("author")
      ));

      Log.d(TAG, "post keys " + post.keySet().toString());

    }

    // Create a new listing object and add the posts, before, and after ids to it
    PostListing listing = new PostListing((String) listingData.get("before"),
        (String) listingData.get("after"), posts);

    Log.d(TAG, listingData.get("after").toString());
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

}
