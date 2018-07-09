package com.relic;


import android.content.Context;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.relic.data.Authenticator;
import com.relic.data.VolleyQueue;
import com.relic.presentation.callbacks.AuthenticationCallback;
import com.relic.presentation.displaysubs.DisplaySubsView;

public class MainActivity extends AppCompatActivity implements AuthenticationCallback {
  final String TAG = "MAIN_ACTIVITY";
  Authenticator auth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // initialize the request queue and authenticator instance
    VolleyQueue.init(getApplicationContext());
    auth = Authenticator.getAuthenticator(this);
    initializeDefaultView();

    if (!auth.isAuthenticated()) {
      // create the login fragment for the user if not authenticated
      LoginFragment loginFragment = new LoginFragment();
      getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_content_frame, loginFragment).commit();
    }

    setSupportActionBar(findViewById(R.id.my_toolbar));
    // set Title of app using custom title textview instead of the default
    getSupportActionBar().setTitle(" ");
    ((Chip) findViewById(R.id.my_toolbar_title)).setChipText(getText(R.string.app_name));

    findViewById(R.id.my_toolbar).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(getApplicationContext(), "NAVBAR", Toast.LENGTH_SHORT).show();
      }
    });
  }


  @Override
  public void onAuthenticated() {
    // sends user to default view of subreddits
    initializeDefaultView();
  }

  public void initializeDefaultView() {
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


