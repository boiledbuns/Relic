package com.relic;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.relic.data.Authenticator;


public class LoginFragment extends Fragment {
  final String TAG = "LOGIN_FRAGMENT";
  View rootView;
  Authenticator auth;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.web_auth, container, false);

    auth = new Authenticator(this.getContext());

    // proceeds with authentication if the user is not already logged in
    if (!auth.checkAuth()) {
      WebView webView = rootView.findViewById(R.id.auth_web_view);
      // sets client to allow view to open in app
      webView.setWebViewClient(new LoginClient());
      webView.loadUrl(auth.getUrl());
    }
    else {
      Toast.makeText(getContext(), "Signed back in", Toast.LENGTH_SHORT).show();
    }

    return rootView;
  }

  class LoginClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
      String checkUrl = auth.getRedirect();
      // closes the login fragment once the user has successfully been authenticated
      if (url.substring(0, checkUrl.length()).equals(checkUrl)) {
        FragmentActivity parentActivity = getActivity();

        Toast.makeText(parentActivity, "You've been authenticated!", Toast.LENGTH_SHORT).show();
        parentActivity.getSupportFragmentManager().popBackStack();

        // retrieves the access token using the redirect url
        Log.d(TAG, url);
        auth.retrieveAccessToken(url);
      }
      return false;
    }
  }


}
