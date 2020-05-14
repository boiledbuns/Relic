package com.relic.presentation.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.*
import androidx.navigation.NavController
import com.relic.R
import com.relic.data.PostSource
import com.relic.domain.models.AccountModel
import com.relic.domain.models.UserModel
import com.relic.interactor.Contract
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicActivity
import com.relic.presentation.displaypost.DisplayPostFragmentArgs
import com.relic.presentation.displaysub.DisplaySubFragmentArgs
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.displayuser.DisplayUserPreview
import com.relic.presentation.editor.ReplyEditorFragment
import com.relic.presentation.media.DisplayGfycatFragmentArgs
import com.relic.presentation.media.DisplayImageFragmentArgs
import com.relic.presentation.preferences.PreferenceLink
import com.relic.presentation.preferences.PreferencesActivity
import com.relic.presentation.preferences.PreferencesActivity.Companion.KEY_RESULT_PREF_LINKS
import com.relic.presentation.search.subreddit.SubSearchFragment
import com.relic.presentation.search.user.UserSearchFragment
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import com.relic.presentation.util.MediaType
import com.relic.presentation.util.RequestCodes
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.activity_main.*
import navigation.setupWithNavController
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
    private var currentNavController: LiveData<NavController>? = null

    // region lifecycle hooks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme()
        setContentView(R.layout.activity_main)
        relicGD = GestureDetectorCompat(this, GestureDetector.SimpleOnGestureListener())

        bindViewModel(this)

        // set up navigation if none to restore
        if (savedInstanceState == null) {
            setupBottomNav(mainVM.userLiveData.value)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        // wait for bottom navigation bar to restore its state (selected item)
        // before setting up it up again
        setupBottomNav(mainVM.userLiveData.value)
    }

    private fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        mainVM.userLiveData.observe(lifecycleOwner) { handleUser(it) }
        mainVM.accountsLiveData.observe(lifecycleOwner) { accounts -> accounts?.let { handleAccounts(it) } }

        // todo converge into a single source
        postInteractor.navigationLiveData.observe(lifecycleOwner) { handleNavigation(it!!) }
        subredditInteractor.navigationLiveData.observe(lifecycleOwner) { handleNavigation(it!!) }
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

    private fun setupBottomNav(userModel: UserModel?) {
        // use the solution from the google navigation components repo for now since there isn't
        // a clear solution for managing multiple backstacks currently
        currentNavController = bottom_navigation.setupWithNavController(
            fragmentManager = supportFragmentManager,
            containerId = R.id.main_content_frame,
            intent = intent,
            initialPosition = 2,
            showLogin = userModel == null
        )
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

    private fun handleUser(userModel: UserModel?) {
        // once the user has signed in, replace the sign in page with the account page
        // trigger the selection
//        setupBottomNav(userModel)
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
                val args = DisplayPostFragmentArgs(postFullName = navData.postId, subredditName = navData.subredditName).toBundle()
                currentNavController?.value?.navigate(R.id.displayPostFragment, args)
            }
            // navigates to display image on top of current fragment
            is NavigationData.ToImage -> {
                val args = DisplayImageFragmentArgs(url = navData.thumbnail).toBundle()
                currentNavController?.value?.navigate(R.id.displayImageFragment, args)
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
                when(navData.source) {
                    is PostSource.Subreddit -> {
                        val args = DisplaySubFragmentArgs(subName = navData.source.getSourceName()).toBundle()
                        currentNavController?.value?.navigate(R.id.displaySubFragment, args)
                    }
                    is PostSource.Frontpage -> {
                        currentNavController?.value?.navigate(R.id.frontpageFragment)
                    }
                }

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
        when (navMediaData.mediaType) {
            MediaType.Gfycat -> {
                val args = DisplayGfycatFragmentArgs(navMediaData.mediaUrl).toBundle()
                currentNavController?.value?.navigate(R.id.displayImageFragment, args)
            }
            else -> {
                Toast.makeText(baseContext, "Media type $navMediaData doesn't have a handler yet", Toast.LENGTH_SHORT).show()
            }
        }
        // TODO handle additional cases and add support for custom plugins
    }

    private fun openPostReplyEditor(parentFullname: String) {
        // this option is for replying to parent
        // Should also allow user to do it inline, but that can be saved for a later task
        val editorFragment = ReplyEditorFragment.create(parentFullname, true)
        transitionToFragment(editorFragment)
    }

}


