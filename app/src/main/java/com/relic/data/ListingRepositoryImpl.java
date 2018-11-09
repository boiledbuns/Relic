package com.relic.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;

import org.json.simple.parser.JSONParser;

public class ListingRepositoryImpl implements ListingRepository {
  private final String TAG = "COMMENT_REPO";

  private ApplicationDB appDB;

  private MutableLiveData<String> listingKey;


  public ListingRepositoryImpl (Context context) {
    //TODO convert VolleyQueue into a singleton
    appDB = ApplicationDB.getDatabase(context);

    // initialize the listing key
    listingKey = new MutableLiveData<>();
  }


  @Override
  public LiveData<String> getKey() {
    return listingKey;
  }


  @Override
  public void retrieveKey(String key) {
    if (key != null) {
      new RetrieveListingAfterTask(appDB, listingKey).execute(key);
    }
  }

  static class RetrieveListingAfterTask extends AsyncTask<String, Integer, Integer> {
    ApplicationDB appDB;
    MutableLiveData<String> listingKey;
    String key;

    RetrieveListingAfterTask(ApplicationDB appDB, MutableLiveData<String> listingKey) {
      super();
      this.appDB = appDB;
      this.listingKey = listingKey;
    }

    @Override
    protected Integer doInBackground(String... strings) {
      // get the "after" value for the string value
      key = appDB.getListingDAO().getNext(strings[0]);
      return null;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      super.onPostExecute(integer);
      listingKey.setValue(key);
    }
  }

  public LiveData<String> getAfter(String fullName) {
    return appDB.getListingDAO().getAfter(fullName);
  }
}
