package com.relic.presentation.home.frontpage

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.media.DisplayImageFragment
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.SubNavigationData
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.frontpage.*
import timber.log.Timber
import javax.inject.Inject

/**
 * consider changing to DisplayMultiFragment
 * -> encompasses all multireddits
 */
class FrontpageFragment : RelicFragment() {
    @Inject
    lateinit var factory : FrontpageVM.Factory

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    private val frontpageVM: FrontpageVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create() as T
            }
        }).get(FrontpageVM::class.java)
    }

    private lateinit var postAdapter: PostItemAdapter
    private lateinit var frontpageRecyclerView : androidx.recyclerview.widget.RecyclerView

    private var scrollLocked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frontpage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postAdapter = PostItemAdapter(viewPrefsManager, frontpageVM)

        frontpageRecyclerView = frontpagePostsRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = postAdapter
        }

        attachViewListeners()
    }

    private fun attachViewListeners() {
        val swipeRefreshLayout = frontpageSwipeRefreshLayout

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
        frontpageRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // TODO : add animation for loading posts
                // checks if the recycler view can no longer scroll downwards
                if (!recyclerView.canScrollVertically(1) && !scrollLocked) {
                    // lock scrolling until posts are loaded to prevent additional unwanted requests
                    scrollLocked = true
                    frontpageProgress.visibility = View.VISIBLE
                    frontpageVM.retrieveMorePosts(false)
                }
            }
        })
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        // observe the live data list of posts for this subreddit
        frontpageVM.postListLiveData.nonNull().observe(lifecycleOwner) { handlePostsLoaded(it) }
        frontpageVM.subNavigationLiveData.nonNull().observe(lifecycleOwner) { handleNavigation(it) }
    }

    // region live data handlers

    private fun handlePostsLoaded(postModels : List<PostModel>) {
        Timber.d("size of frontpage %s", postModels.size)
        postAdapter.setPostList(postModels)
        frontpageProgress.visibility = View.GONE

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