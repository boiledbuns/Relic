package com.relic.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
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
import kotlinx.android.synthetic.main.web_auth.*
import kotlinx.coroutines.*

class SignInFragment: RelicFragment(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob()

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
        // TODO add exit option to allow user to go back
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
        private val redirectHost = "github.com"

        override fun shouldOverrideUrlLoading(webView: WebView, request : WebResourceRequest): Boolean {
            var override = false
            // closes the login fragment once the user has successfully been authenticated
            if (request.url.host == redirectHost) {
                Toast.makeText(context, "You've been authenticated!", Toast.LENGTH_SHORT).show()
                // retrieves the access token using the redirect url
                auth.retrieveAccessToken(request.url.toString(), this)
                override = true
            }
            return override
        }

        override fun onAuthenticated() {
            val handler = CoroutineExceptionHandler { _, e ->
                // TODO add option to show more details and retry
                Snackbar.make(
                    web_auth_rootview,
                    "Authentication unsuccessful",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }

            launch(Dispatchers.Main + handler) {
                userRepo.retrieveSelf()!!.let { name ->
                    userRepo.addAuthenticatedAccount(name)
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