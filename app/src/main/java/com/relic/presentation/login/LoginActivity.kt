package com.relic.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.relic.R
import com.relic.presentation.base.RelicActivity
import com.relic.presentation.util.RequestCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


class LoginActivity : RelicActivity(), CoroutineScope {
    internal val TAG = "LOGIN_ACTIVITY"

    override val coroutineContext = Dispatchers.Main + SupervisorJob()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val preferencesFragment = SignInFragment.create()
        supportFragmentManager.beginTransaction()
            .replace(R.id.login_content_frame, preferencesFragment)
            .commit()
    }

    companion object {

        fun startForResult(fromActivity : Activity) {
            Intent(fromActivity, LoginActivity::class.java).let { intent ->
                fromActivity.startActivityForResult(intent, RequestCodes.CHANGED_ACCOUNT)
            }
        }
    }
}