package com.relic.presentation.displayuser

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.*
import com.relic.R
import com.relic.data.SortScope
import com.relic.data.SortType
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.DisplaySubMenuHelper
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.displayuser.fragments.PostsTabFragment
import com.relic.presentation.media.DisplayImageFragment
import com.relic.presentation.util.RelicEvent
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user.*
import java.util.*
import javax.inject.Inject

class DisplayUserFragment : RelicFragment() {

    @Inject
    lateinit var factory : DisplayUserVM.Factory

    private val displayUserVM : DisplayUserVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create(username) as T
            }
        }).get(DisplayUserVM::class.java)
    }

    private lateinit var username : String

    private lateinit var pagerAdapter: UserContentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_USERNAME)?.let { username = it }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagerAdapter = UserContentPagerAdapter(childFragmentManager).apply {
            contentFragments.add(PostsTabFragment.create(UserTab.Submitted))
            contentFragments.add(PostsTabFragment.create(UserTab.Comments))
            contentFragments.add(PostsTabFragment.create(UserTab.Saved))
            contentFragments.add(PostsTabFragment.create(UserTab.Upvoted))
            contentFragments.add(PostsTabFragment.create(UserTab.Downvoted))
            contentFragments.add(PostsTabFragment.create(UserTab.Gilded))
            contentFragments.add(PostsTabFragment.create(UserTab.Hidden))
        }

        initializeToolbar(userToolbar as Toolbar)

        userViewPager.adapter = pagerAdapter
        userViewPager.offscreenPageLimit = 1
        userTabLayout.setupWithViewPager(userViewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.display_user_menu, menu)

        // have to first get the reference to the menu in charge of sorting
        val userSortMenu = menu.findItem(DisplaySubMenuHelper.userSortMenuId)?.subMenu

        // inflate only sorting types that have a scope menu
        DisplaySubMenuHelper.sortMethodUserMenuIdsWithScope.forEach { userMenuId ->
            val sortingMethodSubMenu = userSortMenu?.findItem(userMenuId)?.subMenu
            inflater.inflate(R.menu.order_scope_menu, sortingMethodSubMenu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var override = true
        var sortType : SortType?
        var sortScope : SortScope?

        when (item.itemId) {
            // when the sorting type is changed
            R.id.user_sort_hot, R.id.user_sort_top -> {
                sortType = DisplaySubMenuHelper.convertMenuItemToSortType(item.itemId)
                displayUserVM.changeSortingMethod(sortType = sortType)
            }
            // these sorting types don't have a scope, so we send sort method immediately
            R.id.user_sort_new, R.id.user_sort_controversial -> {
                pagerAdapter.getItem(userViewPager.currentItem).resetRecyclerView()
                sortType = DisplaySubMenuHelper.convertMenuItemToSortType(item.itemId)
                displayUserVM.changeSortingMethod(sortType = sortType)
            }
            // when the sorting scope is changed
            R.id.order_scope_hour, R.id.order_scope_day, R.id.order_scope_week,
            R.id.order_scope_month, R.id.order_scope_year, R.id.order_scope_all -> {
                pagerAdapter.getItem(userViewPager.currentItem).resetRecyclerView()
                sortScope = DisplaySubMenuHelper.convertMenuItemToSortScope(item.itemId)
                displayUserVM.changeSortingMethod(sortScope = sortScope)
            }
            else -> override = super.onOptionsItemSelected(item)
        }

        return override
    }

    // region livedata handlers

    private fun initializeToolbar(toolbar : Toolbar) {
        val pActivity = (activity as AppCompatActivity)

        toolbar.apply {
            title = getString(R.string.user_prefix_label, username)
            pActivity.setSupportActionBar(this)
        }

        pActivity.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        displayUserVM.userLiveData.nonNull().observe(lifecycleOwner) { userUserPreview.setUser(it) }
        displayUserVM.navigationLiveData.nonNull().observe(lifecycleOwner) { handleNavigation(it) }
    }

    private fun handleNavigation(navEvent : RelicEvent<NavigationData>) {
        if (!navEvent.consumed) {
            val navigation = navEvent.consume()

            when (navigation) {
                is NavigationData.ToPost -> {
                    val postFragment = DisplayPostFragment.create(
                        navigation.postId,
                        navigation.subredditName,
                        navigation.postSource,
                        enableVisitSub = true
                    )
                    // intentionally because replacing then popping off back stack loses scroll position
                    activity!!.supportFragmentManager.beginTransaction()
                        .add(R.id.main_content_frame, postFragment).addToBackStack(TAG).commit()
                }
                // navigates to display image on top of current fragment
                is NavigationData.ToImage -> {
                    val imageFragment = DisplayImageFragment.create(
                        navigation.thumbnail
                    )
                    activity!!.supportFragmentManager.beginTransaction()
                        .add(R.id.main_content_frame, imageFragment).addToBackStack(TAG).commit()
                }
                // let browser handle navigation to url
                is NavigationData.ToExternal -> {
                    val openInBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(navigation.url))
                    startActivity(openInBrowser)
                }
            }
        }
    }

    // endregion livedata handlers

    companion object {
        val ARG_USERNAME = "arg_username"

        fun create(username : String) : DisplayUserFragment {
            val bundle = Bundle().apply {
                putString(ARG_USERNAME, username)
            }

            return DisplayUserFragment().apply {
                arguments = bundle
            }
        }
    }

    private inner class UserContentPagerAdapter(fm : androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {
        val contentFragmentTitles = listOf(
            UserTab.Submitted,
            UserTab.Comments,
            UserTab.Saved,
            UserTab.Upvoted,
            UserTab.Downvoted,
            UserTab.Gilded,
            UserTab.Hidden
        )

        val contentFragments : ArrayList<PostsTabFragment> = ArrayList()

        override fun getItem(p0: Int) : PostsTabFragment {
            // tells vm which tab is currently being displayed, used for additional options
            displayUserVM.setCurrentTab(contentFragmentTitles[p0])

            return contentFragments[p0]
        }

        override fun getCount(): Int = contentFragments.size

        override fun getPageTitle(position: Int): CharSequence? {
            return contentFragmentTitles[position].tabName
        }
    }
}