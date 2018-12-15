package com.relic.presentation.displaysub

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.data.PostRepository
import com.relic.data.models.PostModel
import com.relic.data.models.SubredditModel
import com.relic.presentation.DisplayImageFragment
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import com.relic.presentation.subinfodialog.SubInfoDialogContract
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_sub.*
import java.lang.Error

class DisplaySubFragment : RelicFragment() {

    companion object {
        private const val ARG_SUBREDDIT_NAME = "arg_subreddit_name"

        fun create(subredditName : String) : DisplaySubFragment {
            return DisplaySubFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SUBREDDIT_NAME, subredditName)
                }
            }
        }
    }

    val displaySubVM: DisplaySubVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent
                        .builder()
                        .repoModule(RepoModule(context!!))
                        .authModule(AuthModule(context!!))
                        .build()
                        .getDisplaySubVM()
                        .create(PostRepository.PostSource.Subreddit(subName)) as T
            }
        }).get(DisplaySubVM::class.java)
    }

    private lateinit var subName: String

    private lateinit var searchView: SearchView
    private lateinit var postAdapter: PostItemAdapter

    private var fragmentOpened: Boolean = false
    private var scrollLocked: Boolean = false
    private var tempSortMethod = PostRepository.SortType.DEFAULT

    // region fragment lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.apply {
            subName = getString(ARG_SUBREDDIT_NAME, "")
        }

        postAdapter = PostItemAdapter(displaySubVM)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.display_sub, container, false)
        val recyclerView = root.findViewById<RecyclerView>(R.id.subPostsRecyclerView)
        val toolbar = root.findViewById<Toolbar>(R.id.subToolbar)

        recyclerView.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(context)
        }

        toolbar.apply {
            title = subName
            subtitle = "Sorting by new"
            (activity as AppCompatActivity).setSupportActionBar(this)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeOnClicks()
        attachScrollListeners()

        if (fragmentOpened) {
            fragmentOpened = false
            subAppBarLayout.setExpanded(false, false)
        }

        // adds listener for state change for the appbarlayout issue that always opens it when
        // returning to this fragment after popping another off of the stack
        subAppBarLayout.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {
                if (fragmentOpened) {
                    subAppBarLayout.setExpanded(false)
                    fragmentOpened = false
                }
            }

            override fun onViewDetachedFromWindow(view: View) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.display_sub_menu, menu)

        // have to first get the reference to the menu in charge of sorting
        val sortMenu = menu?.findItem(DisplaySubMenuHelper.sortMenuId)?.subMenu

        // inflate only sorting types that have a sub scope menu
        DisplaySubMenuHelper.sortMethodSubMenuIdsWithScope.forEach { subMenuId ->
            val sortingMethodSubMenu = sortMenu?.findItem(subMenuId)?.subMenu
            inflater?.inflate(R.menu.order_scope_menu, sortingMethodSubMenu)
        }

        menu?.findItem(R.id.display_sub_searchitem)?.let {
            searchView = (it.actionView as SearchView).apply {
                val padding = resources.getDimension(R.dimen.search_padding).toInt()
                setPadding(0, 0, padding, padding)

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(s: String): Boolean {
                        Toast.makeText(context, "Display sub view $s", Toast.LENGTH_SHORT).show()
                        return false
                    }

                    override fun onQueryTextChange(s: String): Boolean {
                        Toast.makeText(context, "Display sub view $s", Toast.LENGTH_SHORT).show()
                        return false
                    }
                })
            }
        }
    }

    // endregion fragment lifecycle hooks

    // region fragment event hooks

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true
        when (item?.itemId) {
            R.id.display_sub_searchitem -> { }
            // when the sorting type is changed
            R.id.post_sort_hot, R.id.post_sort_rising, R.id.post_sort_top -> {
                // update the temporary local sorting method since we don't sort yet
                tempSortMethod = DisplaySubMenuHelper.convertMenuItemToSortType(item.itemId)
            }
            // these sorting types don't have a scope, so we send sort method immediately
            R.id.post_sort_new, R.id.post_sort_best, R.id.post_sort_controversial -> {
                resetRecyclerView()
                val sortType = DisplaySubMenuHelper.convertMenuItemToSortType(item.itemId)
                displaySubVM.changeSortingMethod(sortType)
            }
            // when the sorting scope is changed
            R.id.order_scope_hour, R.id.order_scope_day, R.id.order_scope_week,
            R.id.order_scope_month, R.id.order_scope_year, R.id.order_scope_all -> {
                resetRecyclerView()

                val sortScope = DisplaySubMenuHelper.convertMenuItemToSortScope(item.itemId)
                displaySubVM.changeSortingMethod(sortType = tempSortMethod, sortScope = sortScope)
                Toast.makeText(context, "Sorting option selected $sortScope", Toast.LENGTH_SHORT).show()
            }
            else -> override = super.onOptionsItemSelected(item)
        }

        return override
    }

    // endregion fragment event hooks

    override fun bindViewModel(lifecycleOwner : LifecycleOwner) {
        displaySubVM.postListLiveData.nonNull().observe (lifecycleOwner) { updateLoadedPosts(it) }
        displaySubVM.subredditLiveData.nonNull().observe(lifecycleOwner) { updateSubInfo(it) }
        displaySubVM.navigationLiveData.nonNull().observe(lifecycleOwner) { handleNavigation(it) }
        displaySubVM.subInfoLiveData.nonNull().observe (lifecycleOwner) { setSubInfoData(it) }
    }

    // region LiveData handlers

    private fun updateLoadedPosts(postModels : List<PostModel>) {
        Log.d(TAG, "size of " + postModels.size)
        postAdapter.setPostList(postModels)

        // unlock scrolling to allow more posts to be loaded
        scrollLocked = false
        subSwipeRefreshLayout.isRefreshing = false
    }

    private fun updateSubInfo(subredditModel : SubredditModel) {
        subscribeButtonView.apply {
            text = if (subredditModel.isSubscribed) "subbed" else "subscribe"
        }

        subscribeButtonView.setOnClickListener {
            displaySubVM.updateSubStatus(!subredditModel.isSubscribed)
        }
    }

    private fun handleNavigation(navigationData: NavigationData) {
        when (navigationData) {
            // navigates to display post
            is NavigationData.ToPost -> {
                val postFragment = DisplayPostFragment.create(
                    navigationData.postId,
                    navigationData.subredditName
                )
                // intentionally because replacing then popping off back stack loses scroll position
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_content_frame, postFragment).addToBackStack(TAG).commit()
                fragmentOpened = true
            }
            // navigates to display image on top of current fragment
            is NavigationData.ToImage -> {
                val imageFragment = DisplayImageFragment.create(
                    navigationData.thumbnail
                )
                activity!!.supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, imageFragment).addToBackStack(TAG).commit()
            }
            // let browser handle navigation to url
            is NavigationData.ToExternal -> {
                val openInBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(navigationData.url))
                startActivity(openInBrowser)
            }
        }
    }

    private fun setSubInfoData(sortingInfo : DisplaySubInfoData) {
        val method = DisplaySubMenuHelper.convertSortingTypeToText(sortingInfo.sortingMethod)
        val scope = DisplaySubMenuHelper.convertSortingScopeToText(sortingInfo.sortingScope)

        val sortInfoText = if (scope != null) {
            resources.getString(R.string.sort_by_info, method, scope)
        } else {
            resources.getString(R.string.sort_by_info_no_scope, method)
        }

        subSortByInfo.text = sortInfoText
    }
    // endregion LiveData handlers

    // region view functions

    private fun initializeOnClicks() {
        // set onclick to display sub info when the title is clicked
        subToolbar.setOnClickListener {
            SubInfoBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putString(SubInfoDialogContract.ARG_SUB_NAME, subName)
                }
                show(this@DisplaySubFragment.fragmentManager, TAG)
            }
        }
    }

    private fun attachScrollListeners() {
        // attach listener for checking if the user has scrolled to the bottom of the recyclerview
        subPostsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // checks if the recycler view can no longer scroll downwards
                if (!recyclerView.canScrollVertically(1) && !scrollLocked) {
                    // lock scrolling until set of posts are loaded to prevent additional unwanted retrievals
                    scrollLocked = true
                    // TODO : add animation for loading posts

                    // fetch the next post listing
                    displaySubVM.retrieveMorePosts(false)
                }
            }
        })

        // Attach listener for refreshing the sub
        subSwipeRefreshLayout.setOnRefreshListener {
            resetRecyclerView()
            // tells vm to clear the posts -> triggers action to retrieve more
            displaySubVM.retrieveMorePosts(true)
        }
    }

    private fun resetRecyclerView() {
        // empties current items to show that it's being refreshed
        subPostsRecyclerView.layoutManager?.scrollToPosition(0)
        postAdapter.clear()

        subAppBarLayout.setExpanded(true)
        subSwipeRefreshLayout.isRefreshing = true
    }
}
