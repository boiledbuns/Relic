package com.relic.presentation.home.frontpage

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.UtilModule
import com.relic.data.models.PostModel
import com.relic.presentation.media.DisplayImageFragment
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.SubNavigationData
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.frontpage.*

class FrontpageFragment : Fragment() {
    private val TAG = "FRONTPAGE_VIEW"

    private val frontpageVM: FrontpageVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent
                    .builder()
                    .repoModule(RepoModule(context!!))
                    .authModule(AuthModule(context!!))
                    .utilModule(UtilModule(activity!!.application))
                    .build()
                    .getDisplayFrontpageVM()
                    .create() as T
            }
        }).get(FrontpageVM::class.java)
    }

    private lateinit var postAdapter: PostItemAdapter
    private lateinit var frontpageRecyclerView : RecyclerView

    private var scrollLocked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindViewModel()
        postAdapter = PostItemAdapter(frontpageVM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frontpage, container, false).apply {
            frontpageRecyclerView = findViewById<RecyclerView>(R.id.frontpagePostsRecyclerView).apply {
                itemAnimator = null
                layoutManager = LinearLayoutManager(context)
                adapter = postAdapter
            }

            attachViewListeners(this)
        }
    }

    private fun attachViewListeners(root : View) {
        val swipeRefreshLayout = root.findViewById<SwipeRefreshLayout>(R.id.frontpageSwipeRefreshLayout)

        swipeRefreshLayout.apply {
            setOnRefreshListener {
                // empties current items to show that it's being refreshed
                frontpageRecyclerView.layoutManager!!.scrollToPosition(0)
                postAdapter.clear()
                // tells vm to clear the posts -> triggers action to retrieve more
                frontpageVM.retrieveMorePosts(true)
            }
        }

        // attach listener for checking if the user has scrolled to the bottom of the recycler view
        frontpageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // TODO : add animation for loading posts
                // checks if the recycler view can no longer scroll downwards
                if (!recyclerView.canScrollVertically(1) && !scrollLocked) {
                    // lock scrolling until posts are loaded to prevent additional unwanted requests
                    scrollLocked = true
                    frontpageVM.retrieveMorePosts(false)
                }
            }
        })
    }

    private fun bindViewModel() {
        // observe the live data list of posts for this subreddit
        frontpageVM.postListLiveData.nonNull().observe(this) { handlePostsLoaded(it) }
        frontpageVM.subNavigationLiveData.nonNull().observe(this) { handleNavigation(it) }
    }

    // region live data handlers

    private fun handlePostsLoaded(postModels : List<PostModel>) {
        Log.d(TAG, "size of frontpage" + postModels.size)
        postAdapter.setPostList(postModels)

        // turn off loading animation and unlock scrolling to allow more posts to be loaded
        frontpageSwipeRefreshLayout.isRefreshing = false
        scrollLocked = false
    }

    private fun handleNavigation(subNavigationData: SubNavigationData) {
        when (subNavigationData) {
            // navigates to display post
            is SubNavigationData.ToPost -> {
                val postFragment = DisplayPostFragment.create(
                    postId = subNavigationData.postId,
                    subreddit = subNavigationData.subredditName,
                    postSource = subNavigationData.postSource,
                    enableVisitSub = true
                )
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content_frame, postFragment).addToBackStack(TAG).commit()
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
        }
    }

    // endregion live data handlers


}