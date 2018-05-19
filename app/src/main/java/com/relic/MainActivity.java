package com.relic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.relic.data.Authenticator;

public class MainActivity extends AppCompatActivity {
  Authenticator auth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    auth = new Authenticator(this.getApplicationContext());

    // check if the user is authenticated

    WebView webView = new WebView(this);
    // sets client to allow view to open in app
    webView.setWebViewClient(new LoginClient());
    webView.loadUrl(auth.getUrl());
    setContentView(webView);
  }

  class LoginClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
      String checkUrl = auth.getRedirect();
      // closes app once user logs in
      if (url.substring(0, checkUrl.length()).equals(checkUrl)) {
        onBackPressed();
      }
      return false;
    }

  }

}


