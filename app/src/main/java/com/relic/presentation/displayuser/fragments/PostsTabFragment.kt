package com.relic.presentation.displayuser.fragments

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.domain.models.ListingItem
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displayuser.DisplayUserVM
import com.relic.presentation.displayuser.UserTab
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user_submissions.*
import kotlinx.android.synthetic.main.display_user_submissions.view.*

class PostsTabFragment : RelicFragment(), DisplaySubContract.PostAdapterDelegate {

    private val postsTabVM by lazy {
        ViewModelProviders.of(parentFragment!!).get(DisplayUserVM::class.java)
    }

    private lateinit var selectedUserTab : UserTab

    private lateinit var userPostsAdapter : ListingItemAdapter
    private var scrollLocked: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments!!.getParcelable<UserTab>(ARG_USER_TAB)?.let { userTab ->
            selectedUserTab = userTab
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user_submissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPostsAdapter = ListingItemAdapter(postsTabVM)
        userTabRecyclerView.apply {
            adapter = userPostsAdapter
            layoutManager = LinearLayoutManager(context)
        }
        userTabSwipeRefreshLayout.isRefreshing = true

        attachScrollListeners()
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        // subscribe to the appropriate livedata based on tab selected
        postsTabVM.getTabPostsLiveData(selectedUserTab).nonNull().observe (lifecycleOwner) {
            setListingItems(it)
        }
    }

    private fun attachScrollListeners() {
        // attach listener for checking if the user has scrolled to the bottom of the recyclerview
        userTabRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && !scrollLocked) {
                    // lock scrolling until set of posts are loaded to prevent additional unwanted retrievals
                    scrollLocked = true
                    // fetch the next post listing
                    postsTabVM.requestPosts(selectedUserTab, false)
                }
            }
        })

        userTabSwipeRefreshLayout.setOnRefreshListener {
            resetRecyclerView()
            postsTabVM.requestPosts(selectedUserTab, true)
        }
    }

    private fun setListingItems(listingItems : List<ListingItem>) {
        if (listingItems.isNotEmpty()) {
            userPostsAdapter.setItems(listingItems)
            userTabSwipeRefreshLayout.isRefreshing = false
            scrollLocked = false
        }
    }

    fun resetRecyclerView() {
        // empties current items to show that it's being refreshed
        userTabRecyclerView.layoutManager?.scrollToPosition(0)
        userPostsAdapter.clear()

        userTabSwipeRefreshLayout.isRefreshing = true
    }

    // region delegate functions

    override fun visitPost(postFullname: String, subreddit: String) {}

    override fun voteOnPost(postFullname: String, voteValue: Int) {}

    override fun savePost(postFullname: String, save: Boolean) {}

    override fun onLinkPressed(url: String) {}

    override fun previewUser(username: String) {}

    // endregion delegate functions

    companion object {
        private val ARG_USER_TAB = "arg_user_tab"

        fun create(userTab : UserTab) : PostsTabFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARG_USER_TAB, userTab)

            return PostsTabFragment().apply {
                arguments = bundle
            }
        }
    }
}