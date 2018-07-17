package com.relic;


import android.content.Context;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.relic.data.Authenticator;
import com.relic.data.VolleyQueue;
import com.relic.presentation.callbacks.AuthenticationCallback;
import com.relic.presentation.displaysubs.DisplaySubsView;

public class MainActivity extends AppCompatActivity implements AuthenticationCallback {
  final String TAG = "MAIN_ACTIVITY";
  private Authenticator auth;

  private MenuItem searchMenuItem;
  private SearchView searchView;

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

    findViewById(R.id.my_toolbar).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(getApplicationContext(), "NAVBAR", Toast.LENGTH_SHORT).show();
      }
    });
  }


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
    super.onCreate(savedInstanceState, persistentState);
    // removes custom title
    getSupportActionBar().setTitle("");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
//    getMenuInflater().inflate(R.menu.search_menu, menu);
//
//    searchMenuItem = menu.findItem(R.id.search_item);
//    searchView = (SearchView) searchMenuItem.getActionView();
     return super.onCreateOptionsMenu(menu);
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

  @Override
  public void onBackPressed() {
//    searchView = findViewById(R.id.search_view);
//    // checks if the search view is open
//    if (!searchView.isIconified()) {
//      //searchView.clearFocus();
//      //searchMenuItem.collapseActionView();
//      searchView.setIconified(true);
//    }
//    else {
//      super.onBackPressed();
//    }
    super.onBackPressed();
  }


  public void customSetTitle (String title, String subtitle) {
    ((TextView) findViewById(R.id.my_toolbar_title)).setText(title);
    if (subtitle != null) {
      ((TextView) findViewById(R.id.my_toolbar_subtitle)).setText(subtitle);
    } else {
      ((TextView) findViewById(R.id.my_toolbar_subtitle)).setText("");
    }
  }

  public void customSetTitle (int resId, String subtitle) {
    ((TextView) findViewById(R.id.my_toolbar_title)).setText(resId);
    if (subtitle != null) {
      ((TextView) findViewById(R.id.my_toolbar_subtitle)).setText(subtitle);
    } else {
      ((TextView) findViewById(R.id.my_toolbar_subtitle)).setText("");
    }
  }


  public TextView customGetTitle() {
    return findViewById(R.id.my_toolbar_title);
  }

  public View customGetActionbar() {
    return findViewById(R.id.my_toolbar);
  }
}


