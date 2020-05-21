package com.relic.presentation.home.frontpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.relic.R
import com.relic.data.Auth
import com.relic.data.PostSource
import com.relic.domain.models.PostModel
import com.relic.interactor.Contract
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.DisplaySubVM
import com.relic.presentation.displaysub.list.PostItemAdapter
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
    lateinit var factory : DisplaySubVM.Factory

    @Inject
    lateinit var postInteractor : Contract.PostAdapterDelegate

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    @Inject
    lateinit var auth: Auth

    private val frontpageVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create(PostSource.Frontpage) as T
            }
        }).get(DisplaySubVM::class.java)
    }

    private lateinit var postAdapter: PostItemAdapter
    private lateinit var frontpageRecyclerView : RecyclerView

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
        postAdapter = PostItemAdapter(viewPrefsManager, postInteractor).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        frontpageRecyclerView = frontpagePostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }

        attachViewListeners()
    }

    private fun attachViewListeners() {
        frontpageSwipeRefreshLayout.apply {
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
                    frontpageProgress.visibility = View.VISIBLE
                    frontpageVM.retrieveMorePosts(false)
                }
            }
        })
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        // observe the live data list of posts for this subreddit
        frontpageVM.postListLiveData.observe(lifecycleOwner) { handlePosts(it) }
        frontpageVM.refreshLiveData.observe (lifecycleOwner) { handleRefresh(it) }
    }

    // region live data handlers

    private fun handlePosts(posts : List<PostModel>?) {
        posts?.let {
            Timber.d("size of frontpage %s", posts.size)
            postAdapter.setPostList(posts)

            // unlock scrolling to allow more posts to be loaded
            frontpageProgress.visibility = View.GONE
            scrollLocked = false
        }
    }

    private fun handleRefresh(refreshing : Boolean?) {
        // niche case to hide loading on first open of app when user is not logged in
        if (auth.isAuthenticated()) {
            refreshing?.let { frontpageSwipeRefreshLayout.isRefreshing = it }
        }
    }

    // endregion live data handlers
}