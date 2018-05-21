package com.relic;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // TODO check if user is already logged in first
    LoginFragment loginFragment = new LoginFragment();
    getSupportFragmentManager().beginTransaction().addToBackStack("AUTH")
        .replace(R.id.main_content_frame, loginFragment).commit();
  }

}


