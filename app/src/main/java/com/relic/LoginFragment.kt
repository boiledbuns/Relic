package com.relic

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

import com.relic.data.Authenticator
import com.relic.data.UserRepository
import com.relic.data.UserRepositoryImpl
import com.relic.network.NetworkRequestManager
import com.relic.presentation.callbacks.AuthenticationCallback
import com.relic.presentation.home.HomeFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


class LoginFragment : Fragment() {
    internal val TAG = "LOGIN_FRAGMENT"
    lateinit var rootView: View
    lateinit var auth: Authenticator

    @Inject
    lateinit var userRepo: UserRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.web_auth, container, false)

        // TODO convert to injection
        auth = Authenticator(this.context)
        userRepo = UserRepositoryImpl(this.context!!, NetworkRequestManager(context!!))

        // proceeds with authentication if the user is not already logged in
        if (!auth.isAuthenticated) {
            val webView = rootView.findViewById<WebView>(R.id.auth_web_view)
            // sets client to allow view to open in app
            webView.webViewClient = LoginClient()
            webView.loadUrl(auth.url)
        } else {
            Toast.makeText(context, "Signed back in", Toast.LENGTH_SHORT).show()
        }

        return rootView
    }


    internal inner class LoginClient : WebViewClient(), AuthenticationCallback {
        var parentActivity: FragmentActivity? = null

        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            val checkUrl = auth.redirect
            // closes the login fragment once the user has successfully been authenticated
            if (url.substring(0, checkUrl.length) == checkUrl) {
                parentActivity = activity

                Toast.makeText(parentActivity, "You've been authenticated!", Toast.LENGTH_SHORT).show()
                // retrieves the access token using the redirect url
                Log.d(TAG, url)
                auth.retrieveAccessToken(url, this)
            }
            return false
        }

        override fun onAuthenticated() {
            GlobalScope.launch {
                val deferredUsername = async {
                    userRepo.retrieveSelf()
                }

                auth.initializeUser(deferredUsername.await())
            }
            // sends user to main screen of the app
            parentActivity!!.apply {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content_frame, HomeFragment()).commit()
            }
        }
    }
}
