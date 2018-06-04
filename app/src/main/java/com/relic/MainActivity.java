package com.relic;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.relic.data.Authenticator;
import com.relic.data.VolleyQueue;
import com.relic.presentation.displaysubs.DisplaySubsView;

public class MainActivity extends AppCompatActivity {
  final String TAG = "MAIN_ACTIVITY";
  Authenticator auth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // initialize the request queue
    VolleyQueue.init(getApplicationContext());

    auth = Authenticator.getAuthenticator(this);
    initializeDefaultView();

    if (!auth.isAuthenticated()) {
      // create the login fragment for the user if not authenticated
      LoginFragment loginFragment = new LoginFragment();
      getSupportFragmentManager().beginTransaction().addToBackStack("AUTH")
        .replace(R.id.main_content_frame, loginFragment).commit();
    }
  }


  public void initializeDefaultView() {
    auth.refreshToken();

    // get the number of additional (non default) fragments in the stack
    int fragCount = getSupportFragmentManager().getBackStackEntryCount();
    Log.d(TAG, "Number of fragments " +  fragCount);

    // add the default view only if there are no additional fragments on the stack
    if (fragCount < 1) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_content_frame, new DisplaySubsView()).commit();
    }
  }

}


