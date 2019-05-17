package com.relic

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
import android.widget.TextView
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.UtilModule

import com.relic.data.auth.AuthenticatorImpl
import com.relic.data.models.AccountModel
import com.relic.data.models.UserModel
import com.relic.presentation.callbacks.AuthenticationCallback
import com.relic.presentation.displayuser.DisplayUserFragment
import com.relic.presentation.home.HomeFragment
import com.relic.presentation.login.LoginActivity
import com.relic.presentation.preferences.PreferenceLink
import com.relic.presentation.preferences.PreferencesActivity
import com.relic.presentation.preferences.PreferencesActivity.Companion.KEY_RESULT_PREF_LINKS
import com.relic.util.PreferencesManagerImpl
import com.relic.util.RequestCodes
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_header.*

import javax.inject.Inject

class MainActivity : AppCompatActivity(), AuthenticationCallback {
    internal val TAG = "MAIN_ACTIVITY"

    private val mainVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent.builder()
                    .repoModule(RepoModule(this@MainActivity))
                    .authModule(AuthModule(this@MainActivity))
                    .utilModule(UtilModule(this@MainActivity.application))
                    .build()
                    .getMainVM()
                    .create() as T
            }
        }).get(MainVM::class.java)
    }

    @Inject
    internal lateinit var auth: AuthenticatorImpl

    private lateinit var relicGD: GestureDetectorCompat

    private var itemSelectedDelegate : ((item: MenuItem?) -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme()
        setContentView(R.layout.activity_main)
        initializeDefaultView()

        (application as RelicApp).appComponent.inject(this)

        bindViewModel(this)
        initNavDrawer()

        relicGD = GestureDetectorCompat(this, GestureDetector.SimpleOnGestureListener())
    }

    private fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        mainVM.userLiveData.nonNull().observe (lifecycleOwner) { setUser(it) }
        mainVM.accountsLiveData.nonNull().observe (lifecycleOwner) { setAccounts(it) }
    }

    private fun initNavDrawer() {
        navigationView.setNavigationItemSelectedListener { handleNavMenuOnclick(it) }

        // TODO remove hardcoded username and switch to username used by currently logged in user
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.username).apply {
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.CHANGED_PREFERENCES -> {
                data?.getParcelableArrayListExtra<PreferenceLink>(KEY_RESULT_PREF_LINKS)?.let{
                    handlePreferenceChanges(it)
                }
            }
            RequestCodes.CHANGED_ACCOUNT -> {
                mainVM.onUserSelected()
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

    // region livedata handlers

    private fun setUser(userModel : UserModel) {
        username.text = userModel.name
        linkKarma.text = resources.getString(R.string.placeholder_link_karma, userModel.linkKarma)
        commentKarma.text = resources.getString(R.string.placeholder_comment_karma, userModel.linkKarma)
    }

    private fun setAccounts(accounts: List<AccountModel>) {
        // TODO modify the way accounts are displayed based on number of logged in accounts
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


