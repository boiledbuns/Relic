package com.relic.presentation.displaysub

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
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
import com.relic.presentation.DisplayImageFragment
import com.relic.presentation.adapter.ImageOnClick
import com.relic.presentation.adapter.PostItemOnclick
import com.relic.presentation.displaypost.DisplayPostView
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_sub.*
import java.lang.Error

class DisplaySubView : Fragment() {
    private val TAG = "DISPLAYSUB_VIEW"
    private val SCROLL_POSITION = "POSITION"

    val displaySubVM: DisplaySubVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent
                        .builder()
                        .repoModule(RepoModule(context!!))
                        .authModule(AuthModule(context!!))
                        .build()
                        .getDisplaySubVM()
                        .create(subName) as T
            }
        }).get(DisplaySubVM::class.java)
    }

    private lateinit var subName: String

    private var searchView: SearchView? = null

    private lateinit var postAdapter: PostItemAdapter

    private var fragmentOpened: Boolean = false
    private var scrollLocked: Boolean = false
    private var sortType : Int = PostRepository.SORT_DEFAULT


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.getString("SubredditName")?.let {
            subName = it
        }

        bindViewModel()
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.display_sub, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.subPostsRecyclerView)
        val toolbar = root.findViewById<Toolbar>(R.id.subToolbar)

        postAdapter = PostItemAdapter(displaySubVM)
        recyclerView.apply {
            adapter = postAdapter
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
//            (layoutManager as LinearLayoutManager).onRestoreInstanceState(listManagerState)
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

//        // adds listener for state change for the appbarlayout issue that always opens it when
//        // returning to this fragment after popping another off of the stack
//        subAppBarLayout.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
//            override fun onViewAttachedToWindow(view: View) {
//                if (fragmentOpened) {
//                    subAppBarLayout.setExpanded(false)
//                    fragmentOpened = false
//                }
//            }
//
//            override fun onViewDetachedFromWindow(view: View) {}
//        })
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // recreate saved the saved instance instance
        if (savedInstanceState != null) {
            val position = savedInstanceState.getInt(SCROLL_POSITION)
            Log.d(TAG, "Previous position = $position")
            // scroll to the previous position before reconfiguration change
            // Temporary fix until a better solution is found for jumping to last view position on
            if (position == 0) {
                subAppBarLayout.setExpanded(true)
            } else {
                subAppBarLayout.setExpanded(false)
            }
            subPostsRecyclerView.smoothScrollToPosition(position)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.apply {
            inflate(R.menu.display_sub_menu, menu)

            val sortTypeMenu = menu?.getItem(1)?.subMenu

            // inflate only sorting types that have a sub scope menu
            val menuWithSubMenu = listOf(1, 2, 3, 4)
            for (i in menuWithSubMenu) {
                inflate(R.menu.order_scope_menu, sortTypeMenu?.getItem(i)?.subMenu)
            }
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true
        when (item?.itemId) {
            R.id.display_sub_searchitem -> { }
            // when the sorting type is changed
            R.id.post_sort_best, R.id.post_sort_hot, R.id.post_sort_new, R.id.post_sort_rising,
            R.id.post_sort_top, R.id.post_sort_controversial -> {
                sortType = convertSortType(item.itemId)
            }
            // when the sorting scope is changed
            R.id.order_scope_hour, R.id.order_scope_day, R.id.order_scope_week,
            R.id.order_scope_month, R.id.order_scope_year, R.id.order_scope_all -> {

                displaySubVM.changeSortingMethod(sortType, convertSortScope(item.itemId))

                subAppBarLayout.setExpanded(true)
                subSwipeRefreshLayout.isRefreshing = true
                postAdapter.clear()

                Toast.makeText(context, "Sorting option selected $sortType", Toast.LENGTH_SHORT).show()
            }
            else -> {
                override = super.onOptionsItemSelected(item)
            }
        }

        return override
    }



    /**
     * Observe all the livedata exposed by the viewmodel and attach the appropriate event listeners
     */
    private fun bindViewModel() {
        // observe the livedata list of posts for this subreddit
        displaySubVM.postListLiveData.nonNull().observe (this) { postModels ->
            Log.d(TAG, "size of " + postModels.size)
            postAdapter.setPostList(postModels.toMutableList())

            // unlock scrolling to allow more posts to be loaded
            scrollLocked = false
            subSwipeRefreshLayout.isRefreshing = false
        }

        // observe the subreddit model representing this subreddit
        displaySubVM.subredditLivedata.nonNull().observe(this) { subredditModel ->
            subscribeButtonView.apply {
                text = if (subredditModel.isSubscribed) "subbed" else "subscribe"
            }

            subscribeButtonView.setOnClickListener {
                displaySubVM.updateSubStatus(!subredditModel.isSubscribed)
            }
        }

        displaySubVM.navigationLivedata.nonNull().observe(this) { navigationData ->
            if (navigationData is NavigationData.ToPost) {
                val (postId, subredditName) = navigationData
                PostItemOnClick().onClick(postId, subredditName)
            } else if (navigationData is NavigationData.ToImage) {
                val (thumbnail) = navigationData
                OnClickImage().onClick(thumbnail)
            }
        }
    }

    private fun initializeOnClicks() {
        // set onclick to display sub info when the title is clicked
        subToolbar.setOnClickListener {
            val displaySubInfoView = DisplaySubInfoView()
            val bundle = Bundle()
            bundle.putString("name", subName)

            displaySubInfoView.arguments = bundle
            displaySubInfoView.show(activity!!.supportFragmentManager, TAG)
        }
    }

    private fun attachScrollListeners() {
        // attach listener for checking if the user has scrolled to the bottom of the recyclerview
        subPostsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // checks if the recyclerview can no longer scroll downwards
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
            // empties current items to show that it's being refreshed
            subPostsRecyclerView.layoutManager!!.scrollToPosition(0)
            postAdapter.clear()

            // tells vm to clear the posts -> triggers action to retrieve more
            displaySubVM.retrieveMorePosts(true)
        }
    }

    private fun loadBannerImage(bannerUrl: String?) {
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            // does not load image if the banner img string is empty
            try {
                Log.d("DISPLAY_SUB_VIEW", "URL = $bannerUrl")
                //        Picasso.get().load(bannerUrl).fit().centerCrop().into(bannerImageView);
            } catch (e : Error) {
                Log.d("DISPLAY_SUB_VIEW", "Issue loading image " + e.toString())
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // put the first visible item position into the bundle to allow us to get back to it
//        (subPostsRecyclerView.layoutManager as LinearLayoutManager).let {
//            outState.putInt(SCROLL_POSITION, it.findFirstCompletelyVisibleItemPosition())
//        }
//        outState.putParcelable(SCROLL_POSITION, subPostsRecyclerView.layoutManager?.onSaveInstanceState())
    }

    /**
     * Onclick class for clicking on posts
     */
    private inner class PostItemOnClick : PostItemOnclick {
        override fun onClick(postId: String, subreddit: String) {
            // update post to show that it has been visited
            displaySubVM.visitPost(postId)

            // create a new bundle for the post id
            val bundle = Bundle()
            bundle.putString("full_name", postId)
            bundle.putString("subreddit", subreddit)

            val postFrag = DisplayPostView()
            postFrag.arguments = bundle

            activity?.let {
                it.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.main_content_frame, postFrag)
                        .addToBackStack(TAG)
                        .commit()
                // set flag to show that a fragment has opened
                fragmentOpened = true
            }
        }
    }

    /**
     * Onclick class for imageview onclick
     */
    private inner class OnClickImage : ImageOnClick {
        override fun onClick(url: String) {
            // Parses the url type and routes it appropriately
            val urlEnding = url.substring(url.length - 3)
            if (urlEnding == "jpg" || urlEnding == "png" || urlEnding == "gif") {
                // TODO separate and route according to file ext
                // create a new bundle for to pass the image url along
                val bundle = Bundle()
                bundle.putString("image_url", url)

                val displayImageFragment = DisplayImageFragment()
                displayImageFragment.arguments = bundle

                // replace the current fragment with the new display image frag and add it to the frag stack
                activity!!.supportFragmentManager.beginTransaction()
                        .add(R.id.main_content_frame, displayImageFragment).addToBackStack(TAG)
                        .commit()
            } else {
                // open the url in the browser
                val openInBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(openInBrowser)
            }
        }
    }

    companion object {
        fun convertSortType(optionId : Int) : Int {
            return when(optionId) {
                R.id.post_sort_best -> PostRepository.SORT_BEST
                R.id.post_sort_hot -> PostRepository.SORT_HOT
                R.id.post_sort_new -> PostRepository.SORT_NEW
                R.id.post_sort_rising -> PostRepository.SORT_RISING
                R.id.post_sort_top -> PostRepository.SORT_TOP
                R.id.post_sort_controversial -> PostRepository.SORT_CONTROVERSIAL
                else -> PostRepository.SORT_DEFAULT
            }
        }

        fun convertSortScope(optionId : Int) : Int{
            return when(optionId) {
                R.id.order_scope_hour -> PostRepository.SCOPE_HOUR
                R.id.order_scope_day -> PostRepository.SCOPE_DAY
                R.id.order_scope_week -> PostRepository.SCOPE_WEEK
                R.id.order_scope_month -> PostRepository.SCOPE_MONTH
                R.id.order_scope_year -> PostRepository.SCOPE_YEAR
                R.id.order_scope_all -> PostRepository.SCOPE_ALL
                else -> PostRepository.SCOPE_NONE
            }
        }
    }
}
