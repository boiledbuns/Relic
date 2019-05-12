package com.relic.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.relic.R
import com.relic.data.Authenticator
import com.relic.data.UserRepository
import com.relic.data.UserRepositoryImpl
import com.relic.network.NetworkRequestManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.callbacks.AuthenticationCallback
import kotlinx.coroutines.*

class SignInFragment: RelicFragment() {

    lateinit var auth : Authenticator
    lateinit var userRepo : UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as FragmentActivity).let {
            auth = Authenticator(it)
            userRepo = UserRepositoryImpl(it, NetworkRequestManager(it))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.web_auth, container, false)?.apply {
            val webView = findViewById<WebView>(R.id.auth_web_view)
            // sets client to allow view to open in app
            webView.webViewClient = LoginClient()
            webView.loadUrl(auth.url)
        }
    }

    companion object {
        fun create() : SignInFragment {
            return SignInFragment()
        }
    }

    private inner class LoginClient : WebViewClient(), AuthenticationCallback {

        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            val checkUrl = auth.redirect
            // closes the login fragment once the user has successfully been authenticated
            if (url.substring(0, checkUrl.length) == checkUrl) {

                Toast.makeText(context, "You've been authenticated!", Toast.LENGTH_SHORT).show()
                // retrieves the access token using the redirect url
                Log.d(TAG, url)
                auth.retrieveAccessToken(url, this)
            }
            return false
        }

        override fun onAuthenticated() {
            activity?.apply {
                setResult(Activity.RESULT_OK, Intent())
                onBackPressed()
            }
        }
    }
}