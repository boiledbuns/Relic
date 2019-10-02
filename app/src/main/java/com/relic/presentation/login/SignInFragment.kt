package com.relic.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.relic.R
import com.relic.data.Auth
import com.relic.data.UserRepository
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.callbacks.AuthenticationCallback
import kotlinx.android.synthetic.main.web_auth.*
import kotlinx.coroutines.*
import javax.inject.Inject

class SignInFragment: RelicFragment(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, e ->
        // TODO handle exception
        // TODO add option to show more details and retry
        Snackbar.make(
            web_auth_rootview,
            "Authentication unsuccessful",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    @Inject
    lateinit var auth : Auth
    @Inject
    lateinit var userRepo : UserRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // TODO add exit option to allow user to go back
        return inflater.inflate(R.layout.web_auth, container, false)?.apply {
            findViewById<WebView>(R.id.auth_web_view).apply {
                // need to clear these so previous logins don't interfere w/ new ones
                CookieManager.getInstance().removeAllCookies {
                    // sets client to allow view to open in app
                    webViewClient = LoginClient()
                    loadUrl(auth.url)
                }
            }
        }
    }

    companion object {
        fun create() : SignInFragment {
            return SignInFragment()
        }
    }

    private inner class LoginClient : WebViewClient(), AuthenticationCallback {
        private val redirectHost = "github.com"

        override fun shouldOverrideUrlLoading(webView: WebView, request : WebResourceRequest): Boolean {
            var override = false
            // closes the login fragment once the user has successfully been authenticated
            if (request.url.host == redirectHost) {
                Toast.makeText(context, "You've been authenticated!", Toast.LENGTH_SHORT).show()
                // retrieves the access token using the redirect url
                launch(Dispatchers.Main) {
                    auth.retrieveAccessToken(request.url.toString(), this@LoginClient)
                }
                override = true
            }
            return override
        }

        override fun onAuthenticated() {
            launch(Dispatchers.Main) {
                userRepo.retrieveUsername()?.let { name ->
                    userRepo.getCurrentUser()
                    userRepo.setCurrentAccount(name)
                }

                activity?.apply {
                    setResult(Activity.RESULT_OK, Intent())
                    onBackPressed()
                }
            }
        }
    }
}