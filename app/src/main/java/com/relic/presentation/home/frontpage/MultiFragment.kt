package com.relic.presentation.home.frontpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.relic.R
import com.relic.data.Auth
import com.relic.data.PostSource
import com.relic.domain.models.PostModel
import com.relic.interactor.Contract
import com.relic.interactor.PostInteraction
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.DisplaySubVM
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.relic.presentation.displaysub.list.PostItemsTouchHelper
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.frontpage.*
import javax.inject.Inject

/**
 * consider changing to DisplayMultiFragment
 * -> encompasses all multireddits
 */
class MultiFragment : RelicFragment() {
    @Inject
    lateinit var factory : DisplaySubVM.Factory

    @Inject
    lateinit var postInteractor : Contract.PostAdapterDelegate

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    @Inject
    lateinit var auth: Auth

    private val multiVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val postSource = when(args.multiName) {
                    PostSource.Frontpage.getSourceName() -> PostSource.Frontpage
                    PostSource.All.getSourceName() -> PostSource.All
                    PostSource.Popular.getSourceName() -> PostSource.Popular
                    else -> null
                }
                return factory.create(postSource!!) as T
            }
        }).get(DisplaySubVM::class.java)
    }

    private val args: MultiFragmentArgs by navArgs()

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
            itemAnimator = null
        }

        attachViewListeners()
        val touchHelperCallback = PostItemsTouchHelper(requireContext()) { vh, direction ->
            handleVHSwipeAction(vh, direction)
        }
        ItemTouchHelper(touchHelperCallback).attachToRecyclerView(frontpagePostsRecyclerView)
    }

    private fun attachViewListeners() {
        frontpageSwipeRefreshLayout.apply {
            setOnRefreshListener {
                // empties current items to show that it's being refreshed
                frontpageRecyclerView.layoutManager!!.scrollToPosition(0)
                postAdapter.clear()
                // tells vm to clear the posts -> triggers action to retrieve more
                multiVM.retrieveMorePosts(true)
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
                    multiVM.retrieveMorePosts(false)
                }
            }
        })
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        // observe the live data list of posts for this subreddit
        multiVM.postListLiveData.observe(lifecycleOwner) { handlePosts(it) }
        multiVM.refreshLiveData.observe (lifecycleOwner) { handleRefresh(it) }
    }

    private fun handleVHSwipeAction(vh: RecyclerView.ViewHolder, direction: Int) {
        val postItemVH = vh as PostItemAdapter.PostItemVH
        val postItem = postAdapter.getPostList()[postItemVH.layoutPosition]
        val interaction = when (direction) {
            ItemTouchHelper.RIGHT -> PostInteraction.Upvote
            else -> PostInteraction.Downvote
        }

        postInteractor.interact(postItem, interaction)
        postAdapter.notifyItemChanged(postItemVH.layoutPosition)
    }

    override fun handleNavReselected(): Boolean {
        // for double click, second click will try to access view when removed
        frontpagePostsRecyclerView ?: return false
        return when (frontpagePostsRecyclerView.canScrollVertically(-1)) {
            true -> {
                // can still scroll up, so reselection should scroll to top
                frontpagePostsRecyclerView.smoothScrollToPosition(0)
                true
            }
            false -> {
                // already at top, we don't handle
                false
            }
        }
    }

    // region live data handlers

    private fun handlePosts(posts : List<PostModel>?) {
        posts?.let { loadedPosts ->
            postAdapter.setPostList(loadedPosts)
            frontpageProgress.visibility = View.GONE

            // unlock scrolling to allow more posts to be loaded
            // if no posts have been loaded -> prevent loading "more"
            if (loadedPosts.isNotEmpty()) {
                scrollLocked = false
            }
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