package com.relic.presentation.displaysub

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import com.relic.R
import com.relic.data.PostSource
import com.relic.data.SortType
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.relic.presentation.displaysub.list.PostItemsTouchHelper
import com.relic.presentation.displayuser.DisplayUserPreview
import com.relic.presentation.editor.NewPostEditorFragment
import com.relic.presentation.main.RelicError
import com.relic.presentation.media.DisplayImageFragment
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import com.relic.presentation.subinfodialog.SubInfoDialogContract
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_sub.*
import javax.inject.Inject

class DisplaySubFragment : RelicFragment() {
    @Inject
    lateinit var factory : DisplaySubVM.Factory

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    val displaySubVM: DisplaySubVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create(PostSource.Subreddit(subName)) as T
            }
        }).get(DisplaySubVM::class.java)
    }

    private lateinit var subName: String

    private lateinit var postAdapter: PostItemAdapter
    private lateinit var subItemTouchHelper : ItemTouchHelper
    private lateinit var touchHelperCallback : ItemTouchHelper.Callback

    private var fragmentOpened: Boolean = false
    private var scrollLocked: Boolean = false
    private var tempSortMethod = SortType.DEFAULT

    // region fragment lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.apply {
            subName = getString(ARG_SUBREDDIT_NAME, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.display_sub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postAdapter = PostItemAdapter(viewPrefsManager, displaySubVM)

        subPostsRecyclerView.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(context)
        }

        initializeToolbar()
        initializeOnClicks()
        attachScrollListeners()
        touchHelperCallback = PostItemsTouchHelper(this, context!!)
        subItemTouchHelper = ItemTouchHelper(touchHelperCallback)
        subItemTouchHelper.attachToRecyclerView(subPostsRecyclerView)
        displaySubFAB.hide()

        if (fragmentOpened) {
            fragmentOpened = false
            subAppBarLayout.setExpanded(false)
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

        menu?.clear()
        inflater?.inflate(R.menu.display_sub_menu, menu)

        // have to first get the reference to the menu in charge of sorting
        val sortMenu = menu?.findItem(DisplaySubMenuHelper.sortMenuId)?.subMenu

        // inflate only sorting types that have a sub scope menu
        DisplaySubMenuHelper.sortMethodSubMenuIdsWithScope.forEach { subMenuId ->
            val sortingMethodSubMenu = sortMenu?.findItem(subMenuId)?.subMenu
            inflater?.inflate(R.menu.order_scope_menu, sortingMethodSubMenu)
        }
    }

    // endregion fragment lifecycle hooks

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true
        when (item?.itemId) {
            R.id.display_sub_searchitem -> {
                val searchFrag = SubSearchFragment.create(PostSource.Subreddit(subName))
                // intentionally because replacing then popping off back stack loses scroll position
                activity!!.supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, searchFrag).addToBackStack(TAG).commit()
                fragmentOpened = true
            }
            R.id.display_sub_create_post -> {
                val createPostFrag = NewPostEditorFragment.create(subName)
                // intentionally because replacing then popping off back stack loses scroll position
                activity!!.supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, createPostFrag).addToBackStack(TAG).commit()
                fragmentOpened = true
            }
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
                subSwipeRefreshLayout.isRefreshing = true
            }
            // when the sorting scope is changed
            R.id.order_scope_hour, R.id.order_scope_day, R.id.order_scope_week,
            R.id.order_scope_month, R.id.order_scope_year, R.id.order_scope_all -> {
                resetRecyclerView()

                val sortScope = DisplaySubMenuHelper.convertMenuItemToSortScope(item.itemId)
                displaySubVM.changeSortingMethod(sortType = tempSortMethod, sortScope = sortScope)
                subSwipeRefreshLayout.isRefreshing = true
                Toast.makeText(context, "Sorting option selected $sortScope", Toast.LENGTH_SHORT).show()
            }
            else -> override = super.onOptionsItemSelected(item)
        }

        return override
    }

    override fun bindViewModel(lifecycleOwner : LifecycleOwner) {
        displaySubVM.postListLiveData.nonNull().observe (lifecycleOwner) { updateLoadedPosts(it) }
        displaySubVM.subredditLiveData.nonNull().observe(lifecycleOwner) { updateSubInfo(it) }
        displaySubVM.subNavigationLiveData.nonNull().observe(lifecycleOwner) { handleNavigation(it) }
        displaySubVM.subInfoLiveData.nonNull().observe (lifecycleOwner) { setSubInfoData(it) }
        displaySubVM.errorLiveData.observe (lifecycleOwner) { handleError(it) }

        displaySubVM.refreshLiveData.nonNull().observe (lifecycleOwner) {
            subSwipeRefreshLayout.isRefreshing = it
        }
    }

    fun handleVHSwipeAction(vh : RecyclerView.ViewHolder, direction : Int) {
        // TODO don't handle manually -> need to check preferences
        when (direction) {
            ItemTouchHelper.LEFT -> {
                postAdapter.onPostDownvotePressed(vh.adapterPosition)
            }
            ItemTouchHelper.RIGHT -> {
                postAdapter.onPostUpvotePressed(vh.adapterPosition)
            }
        }
    }

    // region LiveData handlers

    private fun updateLoadedPosts(postModels : List<PostModel>) {
        postAdapter.setPostList(postModels)
        // updates post list size info
//        sub(postModels.size)

        // unlock scrolling to allow more posts to be loaded
        scrollLocked = false
        displaySubProgress.visibility = View.GONE
    }

    private fun updateSubInfo(subredditModel : SubredditModel) {
        subscribeButtonView.apply {
            text = if (subredditModel.isSubscribed) "subbed" else "subscribe"
        }

        subscribeButtonView.setOnClickListener {
            displaySubVM.updateSubStatus(!subredditModel.isSubscribed)
        }
    }

    // TODO consider extracting this code outside of both this and the displaysubfragment
    private fun handleNavigation(subNavigationData: SubNavigationData) {
        when (subNavigationData) {
            // navigates to display post
            is SubNavigationData.ToPost -> {
                val postFragment = DisplayPostFragment.create(
                    subNavigationData.postId,
                    subNavigationData.subredditName,
                    subNavigationData.postSource
                )
                // intentionally because replacing then popping off back stack loses scroll position
                activity!!.supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, postFragment).addToBackStack(TAG).commit()
                fragmentOpened = true
            }
            // navigates to display image on top of current fragment
            is SubNavigationData.ToImage -> {
                val imageFragment = DisplayImageFragment.create(
                    subNavigationData.thumbnail
                )
                activity!!.supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, imageFragment).addToBackStack(TAG).commit()
            }
            // let browser handle navigation to url
            is SubNavigationData.ToExternal -> {
                val openInBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(subNavigationData.url))
                startActivity(openInBrowser)
            }
            is SubNavigationData.ToUserPreview -> {
                DisplayUserPreview.create(subNavigationData.username)
                    .show(this@DisplaySubFragment.fragmentManager, TAG)
            }
        }
    }

    private fun handleError(error : RelicError?) {
        error?.let {
            // cancellation should also stop all progress indicators
            subSwipeRefreshLayout.isRefreshing = false
            displaySubProgress.visibility = View.GONE
            scrollLocked = false

            // default snackbar details for unhandled errors
            var message = resources.getString(R.string.unknown_error)
            var displayLength = Snackbar.LENGTH_SHORT
            var actionMessage: String? = null
            var action: () -> Unit = {}

            when (it) {
                is RelicError.NetworkUnavailable -> {
                    message = resources.getString(R.string.network_unavailable)
                    displayLength = Snackbar.LENGTH_INDEFINITE
                    actionMessage = resources.getString(R.string.refresh)
                    action = { displaySubVM.retrieveMorePosts(true) }
                }
            }

            snackbar = Snackbar.make(displaySubRootView, message, displayLength).apply {
                actionMessage?.let { setAction(actionMessage) { action() } }

                //  TODO extract this into a common util class in presentation
                val typedValue = TypedValue()
                context.theme.resolveAttribute(R.attr.relicTitleColor, typedValue, true);
                val color = typedValue.data;

                setActionTextColor(color)
                show()
            }
        } ?: snackbar?.dismiss()
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

    private fun initializeToolbar() {
        val pActivity = (activity as AppCompatActivity)

        (subToolbar as Toolbar).apply {
            pActivity.setSupportActionBar(this)

            title = subName
            subtitle = "Sorting by new"
            setNavigationOnClickListener { activity?.onBackPressed() }
        }

        pActivity.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
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
                    displaySubProgress.visibility = View.VISIBLE

                    // fetch the next post listing
                    displaySubVM.retrieveMorePosts(false)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0) onScrollUp() else onScrollDown()
            }
        })

        // Attach listener for refreshing the sub
        subSwipeRefreshLayout.setOnRefreshListener {
            resetRecyclerView()
            // tells vm to clear the posts -> triggers action to retrieve more
            displaySubVM.retrieveMorePosts(true)
        }
    }

    // region handle recyclerview scroll listeners

    private fun onScrollUp() {
        displaySubFAB.apply {
            show()
            setOnClickListener {
                subPostsRecyclerView.smoothScrollToPosition(0)
//                subAppBarLayout.setExpanded(true)
            }
        }
    }

    private fun onScrollDown() {
        displaySubFAB.hide()
    }

    // endregion handle recyclerview scroll listeners

    private fun resetRecyclerView() {
        // empties current items to show that it's being refreshed
        subPostsRecyclerView.layoutManager?.scrollToPosition(0)
        postAdapter.clear()

        subAppBarLayout.setExpanded(true)
    }

    // endregion view functions

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
}
