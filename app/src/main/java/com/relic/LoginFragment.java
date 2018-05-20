package com.relic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.relic.data.Authenticator;


public class LoginFragment extends Fragment{
  View rootView;
  Authenticator auth;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    rootView = inflater.inflate(R.layout.web_auth, container);

    auth = new Authenticator(this.getContext());

    // TODO check if the user is authenticated

    WebView webView = rootView.findViewById(R.id.auth_web_view);
    // sets client to allow view to open in app
    webView.setWebViewClient(new LoginClient());
    webView.loadUrl(auth.getUrl());

    return rootView;
  }

  class LoginClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
      String checkUrl = auth.getRedirect();
      // closes the login fragment once the user has successfully been authenticated
      if (url.substring(0, checkUrl.length()).equals(checkUrl)) {
        getActivity().getFragmentManager().popBackStack();
      }
      return false;
    }

  }


}
