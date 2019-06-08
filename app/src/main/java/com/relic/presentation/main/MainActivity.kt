package com.relic.presentation.main

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.widget.DrawerLayout

import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.relic.R

import com.relic.domain.models.AccountModel
import com.relic.domain.models.UserModel
import com.relic.presentation.displayuser.DisplayUserFragment
import com.relic.presentation.home.HomeFragment
import com.relic.presentation.login.LoginActivity
import com.relic.presentation.preferences.PreferenceLink
import com.relic.presentation.preferences.PreferencesActivity
import com.relic.presentation.preferences.PreferencesActivity.Companion.KEY_RESULT_PREF_LINKS
import com.relic.preference.PreferencesManagerImpl
import com.relic.presentation.base.RelicActivity
import com.relic.presentation.util.RequestCodes
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.activity_main.*

import javax.inject.Inject

class MainActivity : RelicActivity() {
    internal val TAG = "MAIN_ACTIVITY"

    @Inject
    lateinit var factory : MainVM.Factory

    private val mainVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create() as T
            }
        }).get(MainVM::class.java)
    }

    private lateinit var relicGD: GestureDetectorCompat

    private var itemSelectedDelegate : ((item: MenuItem?) -> Boolean)? = null
    private lateinit var navHeader : View

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme()
        setContentView(R.layout.activity_main)
        // we can't use ktx on views in the headerview for some reason (ie. it crashes with npe)
        // since i't not super important, just use this workaround for now
        navHeader = navigationView.getHeaderView(0)
        relicGD = GestureDetectorCompat(this, GestureDetector.SimpleOnGestureListener())
        initializeDefaultView()
        initNavDrawer()

        bindViewModel(this@MainActivity)
    }

    private fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        mainVM.userLiveData.observe (lifecycleOwner) { setUser(it) }
        mainVM.accountsLiveData.nonNull().observe (lifecycleOwner) { setAccounts(it) }
    }

    private fun initNavDrawer() {
        navigationView.setNavigationItemSelectedListener { handleNavMenuOnclick(it) }

        // init onclick for user in nav header
        navHeader.findViewById<TextView>(R.id.navHeaderUsername).apply {
            setOnClickListener {
                mainVM.userLiveData.value?.let {
                    val displayUserFrag = DisplayUserFragment.create(text.toString())

                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.main_content_frame, displayUserFrag)
                        .addToBackStack(TAG)
                        .commit()
                } ?: run {
                    LoginActivity.startForResult(this@MainActivity)
                }

                navigationDrawer.closeDrawers()
            }
        }

        // init onclick for "preferences" selector in nav header
        navHeader.findViewById<ImageView>(R.id.navUserDropdownIc).setOnClickListener {
            val dropdown = findViewById<LinearLayout>(R.id.navHeaderDropdown)
            dropdown.visibility = when (dropdown.visibility)  {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
            // init onclick for "preferences" selector in nav header
            navHeader.findViewById<TextView>(R.id.navHeaderAddAccount).setOnClickListener {
                LoginActivity.startForResult(this@MainActivity)
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
            RequestCodes.CHANGED_ACCOUNT -> {
                mainVM.onAccountSelected()
                supportFragmentManager.fragments.forEach {

                }
            }
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

    // endregion lifecycle hooks

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

    // region livedata handlers

    private fun setUser(userModel : UserModel?) {
        navHeader.apply {
            if (userModel != null) {
                findViewById<TextView>(R.id.navHeaderUsername).text = userModel.name
                findViewById<TextView>(R.id.linkKarma).text = resources.getString(R.string.placeholder_link_karma, userModel.linkKarma)
                findViewById<TextView>(R.id.commentKarma).text = resources.getString(R.string.placeholder_comment_karma, userModel.linkKarma)
                findViewById<ImageView>(R.id.navUserDropdownIc).visibility = View.VISIBLE
            } else {
                findViewById<ImageView>(R.id.navUserDropdownIc).visibility = View.GONE
            }
        }
    }

    private fun setAccounts(accounts: List<AccountModel>) {
        navHeader.apply {
            findViewById<LinearLayout>(R.id.navHeaderAccounts).removeAllViews()
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val left = resources.getDimension(R.dimen.padding_xl).toInt()
            val top = resources.getDimension(R.dimen.padding_l).toInt()
            setMargins(left, top, left, 0)
        }

        // create option to change account for each available account
        for (account in accounts) {
            TextView(this).apply {
                text = account.name
                layoutParams = params
                navHeader.findViewById<LinearLayout>(R.id.navHeaderAccounts).addView(this)

                setOnClickListener {
                    // TODO consider switching to preference listener for a cleaner class
                    mainVM.onAccountSelected(account.name)
                    // need to close drawer and dropdown
                    navigationDrawer.closeDrawers()
                    navHeader.findViewById<LinearLayout>(R.id.navHeaderDropdown).visibility = View.GONE

                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.switched_account, account.name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // endregion livedata handlers

    // region navigation view handlers

    private fun handleNavMenuOnclick(item : MenuItem) : Boolean {
        when (item.itemId) {
            R.id.preferences -> PreferencesActivity.startForResult(this)
        }

        navigationDrawer.closeDrawers()
        return true
    }

    // endregion navigation view handlers

    fun getNavDrawer(): DrawerLayout = navigationDrawer!!
}


