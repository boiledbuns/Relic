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
import com.relic.domain.models.PostModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.DisplaySubContract
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
    lateinit var postInteractor : DisplaySubContract.PostAdapterDelegate

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
        postAdapter = PostItemAdapter(viewPrefsManager, postInteractor)

        frontpageRecyclerView = frontpagePostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
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
        frontpageVM.postListLiveData.nonNull().observe(lifecycleOwner) { handlePostsLoaded(it) }
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

    // endregion live data handlers
}