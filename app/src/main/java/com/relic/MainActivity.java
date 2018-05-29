package com.relic;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.relic.data.Authenticator;
import com.relic.data.VolleyQueue;
import com.relic.presentation.DisplaySubs.DisplaySubsView;

public class MainActivity extends AppCompatActivity {
  Authenticator auth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // initialize the request queue
    VolleyQueue.init(getApplicationContext());

    Authenticator auth = new Authenticator(this);

    // take the user to the frontpage
//    getSupportFragmentManager().beginTrans  action()
//        .replace(R.id.main_content_frame, new FrontpageView()).commit();
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_content_frame, new DisplaySubsView()).commit();

    if (!auth.isAuthenticated()) {
      // create the login fragment for the user if not authenticated
      LoginFragment loginFragment = new LoginFragment();
      getSupportFragmentManager().beginTransaction().addToBackStack("AUTH")
        .replace(R.id.main_content_frame, loginFragment).commit();
    }
  }

}


