package com.relic.presentation.displayuser

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import android.view.*
import com.relic.MainActivity
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.data.PostRepository
import com.relic.data.models.UserModel
import com.relic.presentation.DisplayImageFragment
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.DisplaySubMenuHelper
import com.relic.presentation.displaysub.SubNavigationData
import com.relic.presentation.displayuser.fragments.PostsTabFragment
import com.relic.presentation.helper.DateHelper
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user.*
import kotlinx.android.synthetic.main.display_user.view.*
import java.text.SimpleDateFormat
import java.util.*

class DisplayUserFragment : RelicFragment() {

    private lateinit var displayUserVM : DisplayUserVM
    private lateinit var username : String

    private lateinit var pagerAdapter: UserContentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_USERNAME)?.let { username = it }

        displayUserVM = ViewModelProviders.of(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent.builder()
                    .repoModule(RepoModule(context!!))
                    .authModule(AuthModule(context!!))
                    .build()
                    .getDisplayUserVM().create(username) as T
            }
        }).get(DisplayUserVM::class.java)

        pagerAdapter = UserContentPagerAdapter(childFragmentManager).apply {
            contentFragments.add(PostsTabFragment.create(UserTab.Submitted))
            contentFragments.add(PostsTabFragment.create(UserTab.Comments))
            contentFragments.add(PostsTabFragment.create(UserTab.Saved))
            contentFragments.add(PostsTabFragment.create(UserTab.Upvoted))
            contentFragments.add(PostsTabFragment.create(UserTab.Downvoted))
            contentFragments.add(PostsTabFragment.create(UserTab.Gilded))
            contentFragments.add(PostsTabFragment.create(UserTab.Hidden))
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user, container, false).apply {
            (userToolbar as Toolbar).apply {
                title = getString(R.string.user_prefix_label, username)
                (activity as MainActivity).setSupportActionBar(this)
            }

            userViewPager.adapter = pagerAdapter
            userViewPager.offscreenPageLimit = 1
            userTabLayout.setupWithViewPager(userViewPager)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViewModel(viewLifecycleOwner)
        (userToolbar as Toolbar).setNavigationOnClickListener { activity?.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.display_user_menu, menu)

        // have to first get the reference to the menu in charge of sorting
        val userSortMenu = menu?.findItem(DisplaySubMenuHelper.userSortMenuId)?.subMenu

        // inflate only sorting types that have a scope menu
        DisplaySubMenuHelper.sortMethodUserMenuIdsWithScope.forEach { userMenuId ->
            val sortingMethodSubMenu = userSortMenu?.findItem(userMenuId)?.subMenu
            inflater?.inflate(R.menu.order_scope_menu, sortingMethodSubMenu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true
        var sortType : PostRepository.SortType? = null
        var sortScope : PostRepository.SortScope? = null

        when (item?.itemId) {
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

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        displayUserVM.userLiveData.nonNull().observe (lifecycleOwner) { updateUserInfo(it) }
        displayUserVM.navigationLiveData.nonNull().observe (lifecycleOwner) { handleNavigation(it) }
    }

    private fun updateUserInfo(userModel: UserModel) {
        linkKarma.text = userModel.linkKarma.toString()
        commentKarma.text = userModel.commentKarma.toString()
        totalKarma.text = (userModel.linkKarma + userModel.commentKarma).toString()

        userCreated.text = getUserCreatedString(userModel.created.toDouble().toLong())
    }

    private fun handleNavigation(navigation : SubNavigationData) {
        when (navigation) {
            is SubNavigationData.ToPost -> {
                val postFragment = DisplayPostFragment.create(
                    navigation.postId,
                    navigation.subredditName,
                    navigation.postSource
                )
                // intentionally because replacing then popping off back stack loses scroll position
                activity!!.supportFragmentManager.beginTransaction().add(R.id.main_content_frame, postFragment).addToBackStack(TAG).commit()
            }
            // navigates to display image on top of current fragment
            is SubNavigationData.ToImage -> {
                val imageFragment = DisplayImageFragment.create(
                    navigation.thumbnail
                )
                activity!!.supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, imageFragment).addToBackStack(TAG).commit()
            }
            // let browser handle navigation to url
            is SubNavigationData.ToExternal -> {
                val openInBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(navigation.url))
                startActivity(openInBrowser)
            }
        }
    }

    // endregion livedata handlers

    private fun getUserCreatedString(created : Long) : String {
        // initialize the date formatter and date for "now"
        val formatter = SimpleDateFormat("MMM dd',' YYYY", Locale.CANADA)
        val createdDate = Date(created * 1000)

        val userAge = DateHelper.getDateDifferenceString(createdDate, Date())
        val userCreationDate = formatter.format(createdDate)

        return resources.getString(R.string.account_age, userAge, userCreationDate)
    }

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

    private inner class UserContentPagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {
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