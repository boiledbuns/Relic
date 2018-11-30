package com.relic.data;


import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.relic.R;
import com.relic.network.NetworkRequestManager;
import com.relic.data.gateway.PostGateway;
import com.relic.data.gateway.PostGatewayImpl;
import com.relic.network.request.RelicOAuthRequest;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.data.entities.ListingEntity;
import com.relic.data.entities.PostEntity;
import com.relic.data.models.PostModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

// TODO convert to KOTLIN
public class PostRepositoryImpl implements PostRepository {
  private final String ENDPOINT = "https://oauth.reddit.com/";
  private final String userAgent = "android:com.relic.Relic (by /u/boiledbuns)";
  private final String TAG = "POST_REPO";

  private String[] sortByMethods = {"best", "controversial", "hot", "new", "rising", "top"};
  private String[] sortScopes = {"hour", "day", "week", "month", "year", "all"};

  private Context currentContext;
  private JSONParser JSONParser;
  private ApplicationDB appDB;

  private NetworkRequestManager requestManager;

  @Inject
  public PostRepositoryImpl(Context context, NetworkRequestManager networkRequestManager) {
    currentContext = context;
    requestManager = networkRequestManager;

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
    LiveData<List<PostModel>> subredditPosts;
    // handles specific cases
    // try to convert this to enum if time permits
    if (subreddit.isEmpty()) {
      subredditPosts = appDB.getPostDao().getFrontPagePosts();
    } else {
      subredditPosts = appDB.getPostDao().getSubredditPosts(subreddit);
    }
    return subredditPosts;
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
    requestManager.processRequest(new RelicOAuthRequest(
            RelicOAuthRequest.GET,
            ENDPOINT + ending,
            response -> {
              try {
                parsePosts(response, subredditName);
                //new InsertPostsTask(appDB, parsePosts(response, subredditName)).execute();
              }
              catch (ParseException error) {
                Log.d(TAG, "Error: " + error.getMessage());
              }
            },
            error -> Log.d(TAG, "Error: " + error.getMessage()),
            checkToken()
    ));
  }

  public void retrieveSortedPosts(String subredditName, int sortType) {
    retrieveSortedPosts(subredditName, sortType, 0);
  }


  /**
   * Deletes all locally stored posts and retrieves a new set based on the sorting method specified
   * by the caller
   * @param subredditName name of the subreddit for the posts to be retrieved
   * @param sortType code for the associated sort by method
   * @param sortScope  code for the associate time span to sort by
   */
  @Override
  public void retrieveSortedPosts(String subredditName, int sortType, int sortScope) {
    // generate the ending of the request url based on sorting method specified
    String ending = ENDPOINT + "r/" + subredditName;

    // change the endpoint based on which sorting option the user has selected
    if (sortType != PostRepository.SORT_DEFAULT && sortType <= sortByMethods.length) {
      // build the appropriate endpoint based on the "sort by" code and time scope
      ending += "/" + sortByMethods[sortType - 1] + "/?sort=" + sortByMethods[sortType - 1];

      // only add sort scope for these sorting types
      if (sortType == SORT_HOT || sortType == SORT_RISING || sortType == SORT_TOP) {
        // add the scope only if the sorting type has one
        ending += "&t=" + sortScopes[sortScope - 1];
      }
    }

    Log.d(TAG, ending);
    requestManager.processRequest(new RelicOAuthRequest(
            RelicOAuthRequest.GET,
            ending,
            (String response) -> {
              Log.d(TAG, response);
              try {
                parsePosts(response, subredditName);
              } catch (ParseException e) {
                e.printStackTrace();
              }

            },
            error -> {
              Log.d(TAG, "Error retrieving sorted posts " + error);
            },
            checkToken()
    ));
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

    // initialize the date formatter and date for now
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd',' hh:mm a");
    Date current = new Date();

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
      Log.d(TAG, "link_flair_richtext : " + post.get("score") + " "+ post.get("ups") + " "+ post.get("wls") + " "+ post.get("likes"));
      //Log.d(TAG, "link_flair_richtext : " + post.get("visited") + " "+ post.get("views") + " "+ post.get("pwls") + " "+ post.get("gilded"));

      //Log.d(TAG, "post keys " + post.keySet().toString())
      // unmarshall the object and add it into a list
      PostEntity postEntity = gson.fromJson(post.toJSONString(), PostEntity.class);
      Boolean likes = (Boolean) post.get("likes");
      postEntity.userUpvoted = likes == null ? 0 : (likes ? 1 : -1);

      if (subreddit.isEmpty()) {
        postEntity.origin = PostEntity.ORIGIN_FRONTPAGE;
      }

      // TODO create parse class/switch to a more efficient method of removing html
      String authorFlair = (String) post.get("author_flair_text");
      if (authorFlair != null && !authorFlair.isEmpty()) {
        postEntity.author_flair_text = Html.fromHtml(authorFlair).toString();
      } else {
        postEntity.author_flair_text = null;
      }
      Log.d(TAG, "epoch = " + post.get("created"));

      // add year to stamp if the post year doesn't match the current one
      Date created = new Date((long) ((double) post.get("created"))*1000);
      if (current.getYear() != created.getYear()) {
        postEntity.created = created.getYear() + " " + formatter.format(created);
      } else {
        postEntity.created = formatter.format(created);
      }

      postEntities.add(postEntity);
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
    requestManager.processRequest(new RelicOAuthRequest(
            RelicOAuthRequest.GET,
            ending,
            response -> {
                Log.d(TAG, "Loaded response " + response);
                try {
                  PostEntity post = parsePost(response);
                  new InsertPostTask().execute(appDB, post);
                }
                catch (ParseException error) {
                  Log.d(TAG, "Error: " + error.getMessage());
                }
            },
            error -> {
              Log.d(TAG, "Error: "   + error.networkResponse);
              // TODO add livedata for error
              // TODO maybe retry if not an internet connection issue
            },
            checkToken()
    ));
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
    return new PostGatewayImpl(currentContext, requestManager);
  }
}
