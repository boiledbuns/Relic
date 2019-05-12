package com.relic

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.widget.DrawerLayout

import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView

import com.relic.data.Authenticator
import com.relic.network.VolleyQueue
import com.relic.presentation.callbacks.AuthenticationCallback
import com.relic.presentation.displayuser.DisplayUserFragment
import com.relic.presentation.home.HomeFragment
import com.relic.presentation.login.LoginActivity
import com.relic.presentation.login.LoginActivity.Companion.KEY_RESULT_LOGIN
import com.relic.presentation.preferences.PreferenceLink
import com.relic.presentation.preferences.PreferencesActivity
import com.relic.presentation.preferences.PreferencesActivity.Companion.KEY_RESULT_PREF_LINKS
import com.relic.util.PreferencesManagerImpl
import com.relic.util.RequestCodes
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import javax.inject.Inject

class MainActivity : AppCompatActivity(), AuthenticationCallback {
    internal val TAG = "MAIN_ACTIVITY"

    @Inject
    internal lateinit var auth: Authenticator

    private lateinit var navigationView: NavigationView
    private lateinit var navDrawer: DrawerLayout
    private lateinit var relicGD: GestureDetectorCompat

    private var itemSelectedDelegate : ((item: MenuItem?) -> Boolean)? = null
    private var username :String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme()
        setContentView(R.layout.activity_main)
        initializeDefaultView()

        (application as RelicApp).appComponent.inject(this)

        // initialize the request queue and authenticator instance
        VolleyQueue.get(applicationContext)
        auth.refreshToken { this.initializeDefaultView() }

        navigationView = findViewById(R.id.navigationView)
        navDrawer = findViewById(R.id.navigationDrawer)
        username = auth.user
        initNavDrawer()

        relicGD = GestureDetectorCompat(this, GestureDetector.SimpleOnGestureListener())
    }

    private fun initNavDrawer() {
        navigationView.setNavigationItemSelectedListener { handleNavMenuOnclick(it) }

        // TODO remove hardcoded username and switch to username used by currently logged in user
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.username).apply {
            if (username == null) {
                text = resources.getString(R.string.log_in)
                setOnClickListener {
                    // create the login activity for the user
                    LoginActivity.startForResult(this@MainActivity)
                    navDrawer.closeDrawers()
                }
            }
            else {
                text = username
                setOnClickListener {
                    val displayUserFrag = DisplayUserFragment.create(text.toString())

                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.main_content_frame, displayUserFrag)
                        .addToBackStack(TAG)
                        .commit()

                    navDrawer.closeDrawers()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.CHANGED_PREFERENCES -> {
                data?.getParcelableArrayListExtra<PreferenceLink>(KEY_RESULT_PREF_LINKS)?.let{
                    handlePreferenceChanges(it)
                }
            }
            RequestCodes.CHANGED_ACCOUNT -> { }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // need to return true is the touch event is intercepted
        return if (relicGD.onTouchEvent(event)) {
            true
        }
        else {
            super.onTouchEvent(event)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return itemSelectedDelegate?.let {
            it (item)
        } ?: super.onOptionsItemSelected(item)
    }

    // region callback interface

    override fun onAuthenticated() {
        // sends user to default view of subreddits
//        initializeDefaultView()
    }

    // endregion callback interface

    private fun initializeDefaultView() {
        // get the number of additional (non default) fragments in the stack
        val fragCount = supportFragmentManager.backStackEntryCount
        Log.d(TAG, "Number of fragments $fragCount")

        // add the default view only if there are no additional fragments on the stack
        if (fragCount < 1) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_content_frame, HomeFragment())
                    .commit()
        }
    }

    private fun setTheme() {
        val themeId = PreferencesManagerImpl
                .create(getPreferences(Context.MODE_PRIVATE))
                .getApplicationTheme()

        setTheme(themeId)
    }

    private fun handlePreferenceChanges(changedPreferenceLinks : ArrayList <PreferenceLink> ) {
        // TODO handle each set of changed preferences properly when we add new ones
        recreate()
    }

    // region navigation view handlers

    private fun handleNavMenuOnclick(item : MenuItem) : Boolean {
        when (item.itemId) {
            R.id.preferences -> PreferencesActivity.startForResult(this)
        }

        navDrawer.closeDrawers()
        return true
    }

    // endregion navigation view handlers

    fun getNavDrawer() = navDrawer
}


