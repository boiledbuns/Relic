package com.relic;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.relic.data.AccountRepository;
import com.relic.data.AccountRepositoryImpl;
import com.relic.data.Authenticator;
import com.relic.data.VolleyQueue;
import com.relic.presentation.Frontpage.FrontpageView;

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
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_content_frame, new FrontpageView()).commit();

    if (!auth.isAuthenticated()) {
      // create the login fragment for the user if not authenticated
      LoginFragment loginFragment = new LoginFragment();
      getSupportFragmentManager().beginTransaction().addToBackStack("AUTH")
        .replace(R.id.main_content_frame, loginFragment).commit();
    }

    AccountRepository accountRepo = new AccountRepositoryImpl(this);
  }

}


