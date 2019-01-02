package com.relic

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout

import android.util.Log
import android.view.MenuItem
import android.widget.TextView

import com.relic.data.Authenticator
import com.relic.network.VolleyQueue
import com.relic.presentation.callbacks.AuthenticationCallback
import com.relic.presentation.home.HomeFragment
import com.relic.presentation.preferences.PreferenceChangedListener
import com.relic.presentation.preferences.PreferenceLink
import com.relic.presentation.preferences.PreferencesActivity
import com.relic.presentation.preferences.PreferencesActivity.Companion.KEY_RESULT_PREF_LINKS
import com.relic.presentation.preferences.PreferencesFragment
import com.relic.util.PreferencesManagerImpl
import com.relic.util.RequestCodes
import kotlinx.android.synthetic.main.preferences.*

import javax.inject.Inject

class MainActivity : AppCompatActivity(), AuthenticationCallback {
    internal val TAG = "MAIN_ACTIVITY"

    private val titleTW: TextView? = null
    private val subtitleTW: TextView? = null

    @Inject
    internal lateinit var auth: Authenticator

    private lateinit var navigationView: NavigationView
    private lateinit var navDrawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme()
        setContentView(R.layout.activity_main)

        (application as RelicApp).appComponent.inject(this)

        // initialize the request queue and authenticator instance
        VolleyQueue.get(applicationContext)
        auth.refreshToken { this.initializeDefaultView() }

        if (!auth.isAuthenticated) {
            // create the login fragment for the user if not authenticated
            val loginFragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content_frame, loginFragment).commit()
        }

        //    findViewById(R.id.my_toolbar).setOnClickListener(new View.OnClickListener() {
        //      @Override
        //      public void onClick(View view) {
        //        Toast.makeText(getApplicationContext(), "NAVBAR", Toast.LENGTH_SHORT).show();
        //      }
        //    });

        navigationView = findViewById(R.id.navigationView)
        navDrawer = findViewById(R.id.navigationDrawer)
        navigationView.setNavigationItemSelectedListener { handleNavMenuOnclick(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.CHANGED_PREFERENCES -> {
                data?.getParcelableArrayListExtra<PreferenceLink>(KEY_RESULT_PREF_LINKS)?.let{
                    handlePreferenceChanges(it)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // region callback interface

    override fun onAuthenticated() {
        // sends user to default view of subreddits
        initializeDefaultView()
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

}


