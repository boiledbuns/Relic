package com.relic.data;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;

import org.json.simple.parser.JSONParser;

public class ListingRepositoryImpl implements ListingRepository {
  private final String TAG = "COMMENT_REPO";

  private ApplicationDB appDB;
  private RequestQueue queue;


  public ListingRepositoryImpl (Context context) {
    //TODO convert VolleyQueue into a singleton
    appDB = ApplicationDB.getDatabase(context);
    queue = Volley.newRequestQueue(context);
  }

  @Override
  public void getKey(String key, RetrieveNextListingCallback callback) {
    if (key != null) {
      new RetrieveListingAfterTask(appDB, callback).execute(key);
    }
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
      // get the "after" value for the string value
      String subAfter = appDB.getListingDAO().getNext(strings[0]);
      callback.onNextListing(subAfter);
      return null;
    }
  }

}
