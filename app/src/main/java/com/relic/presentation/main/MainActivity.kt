package com.relic.presentation.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.relic.R
import com.relic.domain.models.AccountModel
import com.relic.domain.models.UserModel
import com.relic.interactor.Contract
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicActivity
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.displaysubs.DisplaySubsFragment
import com.relic.presentation.displayuser.DisplayUserFragment
import com.relic.presentation.displayuser.DisplayUserPreview
import com.relic.presentation.editor.ReplyEditorFragment
import com.relic.presentation.home.HomeFragment
import com.relic.presentation.login.LoginFragment
import com.relic.presentation.media.DisplayGfycatFragment
import com.relic.presentation.media.DisplayImageFragment
import com.relic.presentation.preferences.PreferenceLink
import com.relic.presentation.preferences.PreferencesActivity
import com.relic.presentation.preferences.PreferencesActivity.Companion.KEY_RESULT_PREF_LINKS
import com.relic.presentation.search.SearchFragment
import com.relic.presentation.search.subreddit.SubSearchFragment
import com.relic.presentation.search.user.UserSearchFragment
import com.relic.presentation.settings.SettingsFragment
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import com.relic.presentation.util.MediaType
import com.relic.presentation.util.RequestCodes
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : RelicActivity() {
    @Inject
    lateinit var factory: MainVM.Factory

    @Inject
    lateinit var postInteractor: Contract.PostAdapterDelegate

    @Inject
    lateinit var subredditInteractor: Contract.SubAdapterDelegate

    @Inject
    lateinit var viewPrefsManager: ViewPreferencesManager

    private val mainVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create() as T
            }
        }).get(MainVM::class.java)
    }

    private lateinit var relicGD: GestureDetectorCompat

    private var itemSelectedDelegate: ((item: MenuItem?) -> Boolean)? = null



    private val backstack = HashMap<Int, MutableList<Fragment>>().apply {
        put(R.id.nav_account, ArrayList())
        put(R.id.nav_subreddits, ArrayList())
        put(R.id.nav_home, ArrayList())
        put(R.id.nav_search, ArrayList())
        put(R.id.nav_settings, ArrayList())
    }

    private val subsFragment by lazy { DisplaySubsFragment() }
    private val homeFragment by lazy { HomeFragment() }
    private val accountFragment by lazy { DisplayUserFragment.create(null) }
    private val searchFragment by lazy { SearchFragment() }
    private val settingsFragment by lazy { SettingsFragment() }

    // region lifecycle hooks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme()
        setContentView(R.layout.activity_main)
        relicGD = GestureDetectorCompat(this, GestureDetector.SimpleOnGestureListener())

        setupBottomNav()
        bindViewModel(this)
    }

    private fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        mainVM.userLiveData.observe(lifecycleOwner) { setUser(it) }
        mainVM.accountsLiveData.observe(lifecycleOwner) { accounts -> accounts?.let { handleAccounts(it) } }

        postInteractor.navigationLiveData.observe(lifecycleOwner) { handleNavigation(it!!) }
        subredditInteractor.navigationLiveData.observe(lifecycleOwner) { handleNavigation(it!!) }
    }

    private fun setupBottomNav() {
        bottom_navigation.setOnNavigationItemSelectedListener { menuItem ->
//             check if the current back stack is not null
            for (entry in supportFragmentManager.fragments) {
//                // create a new bundle for the fragment and add it to the managed bundle list
//                // associated with the given navigation item
//
//                Bundle().apply {
//                    supportFragmentManager.putFragment(this, currentItem.toString(), entry)
//                    backstack[currentItem]?.add(this)
//                }
                val currentItem = bottom_navigation.selectedItemId
                backstack[currentItem]?.add(entry)
            }
//            val currentItem = bottom_navigation.selectedItemId
//            backstack[currentItem] = supportFragmentManager.fragments.forEach()

            val itemId = menuItem.itemId
            val fragment = when (itemId) {
                R.id.nav_account -> {
                    mainVM.userLiveData.value?.let { accountFragment } ?: LoginFragment()
                }
                R.id.nav_subreddits -> subsFragment
                R.id.nav_home -> homeFragment
                R.id.nav_search -> searchFragment
                R.id.nav_settings -> settingsFragment
                else -> null
            }
            fragment?.let {
                transitionToFragment(it)
            }

            // check if the destination has fragments saved
            val entries = backstack[itemId]!!
            if (entries.isNotEmpty()) {
                val transaction = supportFragmentManager.beginTransaction()
                for (entry in entries) {
                    transaction.replace(R.id.main_content_frame, entry).addToBackStack(entry.tag)
                }
                transaction.commit()
            }
            supportFragmentManager.popBackStack()

            true
        }
        // default page
        bottom_navigation.selectedItemId = R.id.nav_home
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.CHANGED_PREFERENCES -> {
                data?.getParcelableArrayListExtra<PreferenceLink>(KEY_RESULT_PREF_LINKS)?.let {
                    handlePreferenceChanges(it)
                }
            }
            RequestCodes.CHANGED_ACCOUNT -> {
                mainVM.onAccountSelected()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // need to return true is the touch event is intercepted
        return if (relicGD.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return itemSelectedDelegate?.let {
            it(item)
        } ?: super.onOptionsItemSelected(item)
    }

    // endregion lifecycle hooks

    private fun initializeDefaultView() {
        // get the number of additional (non default) fragments in the stack
        val fragCount = supportFragmentManager.backStackEntryCount
        Timber.d("Number of fragments $fragCount")

        // add the default view only if there are no additional fragments on the stack
        if (fragCount < 1) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_content_frame, HomeFragment())
                .commit()
        }
    }

    private fun setTheme() {
        val themeId = viewPrefsManager.getAppTheme()
        setTheme(themeId)
    }

    private fun handlePreferenceChanges(changedPreferenceLinks: ArrayList<PreferenceLink>) {
        // TODO handle each set of changed preferences properly when we add new ones
        recreate()
    }

    // region livedata handlers

    private fun setUser(userModel: UserModel?) {
        // once the user has signed in, replace the sign in page with the account page
        // trigger the selection
        bottom_navigation.selectedItemId = R.id.nav_home
    }

    private fun handleAccounts(accounts: List<AccountModel>) {
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
                // TODO switch to different way of viewing accounts
//                navHeader.findViewById<LinearLayout>(R.id.navHeaderAccounts).addView(this)
//
//                setOnClickListener {
//                    // TODO consider switching to preference listener for a cleaner class
//                    mainVM.onAccountSelected(account.name)
//                    // need to close drawer and dropdown
//                    navigationDrawer.closeDrawers()
//                    navHeader.findViewById<LinearLayout>(R.id.navHeaderDropdown).visibility = View.GONE
//
//                    Toast.makeText(
//                        this@MainActivity,
//                        getString(R.string.switched_account, account.name),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
            }
        }
    }

    // endregion livedata handlers

    // region navigation view handlers

    private fun handleNavMenuOnclick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_subs -> SubSearchFragment.create().apply {
                transitionToFragment(this)
            }
            R.id.search_users -> UserSearchFragment.create().apply {
                transitionToFragment(this)
            }
            R.id.preferences -> PreferencesActivity.startForResult(this)
        }

        return true
    }

    // endregion navigation view handlers

    // region post interactor

    private fun handleNavigation(navData: NavigationData) {
        when (navData) {
            // navigates to display post
            is NavigationData.ToPost -> {
                val postFragment = DisplayPostFragment.create(
                    navData.postId,
                    navData.subredditName
                )
                // intentionally because replacing then popping off back stack loses scroll position
                supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, postFragment)
                    .addToBackStack(TAG)
                    .commit()
            }
            // navigates to display image on top of current fragment
            is NavigationData.ToImage -> {
                val imageFragment = DisplayImageFragment.create(
                    navData.thumbnail
                )
                supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, imageFragment).addToBackStack(TAG).commit()
            }
            // let browser handle navigation to url
            is NavigationData.ToExternal -> {
                val openInBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(navData.url))
                startActivity(openInBrowser)
            }
            is NavigationData.ToUserPreview -> {
                DisplayUserPreview.create(navData.username)
                    .show(supportFragmentManager, TAG)
            }
            is NavigationData.ToPostSource -> {
                val subFragment = DisplaySubFragment.create(navData.source.getSourceName())
                transitionToFragment(subFragment)
            }
            is NavigationData.PreviewPostSource -> {
                SubInfoBottomSheetDialog.create(navData.source.getSourceName())
                    .show(supportFragmentManager, TAG)
            }

            is NavigationData.ToMedia -> openMedia(navData)
            is NavigationData.ToReply -> openPostReplyEditor(navData.parentFullname)
        }
    }

    // endregion post interactor

    private fun openMedia(navMediaData: NavigationData.ToMedia) {
        val displayFragment = when (navMediaData.mediaType) {
            MediaType.Gfycat -> DisplayGfycatFragment.create(navMediaData.mediaUrl)
            else -> DisplayImageFragment.create(navMediaData.mediaUrl)
        }
        supportFragmentManager
            .beginTransaction()
            .add(R.id.main_content_frame, displayFragment)
            .addToBackStack(TAG)
            .commit()
    }

    private fun openPostReplyEditor(parentFullname: String) {
        // this option is for replying to parent
        // Should also allow user to do it inline, but that can be saved for a later task
        val editorFragment = ReplyEditorFragment.create(parentFullname, true)
        transitionToFragment(editorFragment)
    }

}


