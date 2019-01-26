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
import com.relic.data.models.ListingItem
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.relic.presentation.displayuser.DisplayUserVM
import com.relic.presentation.displayuser.UserTab
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user_submissions.*
import kotlinx.android.synthetic.main.display_user_submissions.view.*

class PostsTabFragment : RelicFragment() {

    private lateinit var postsTabVM : DisplayUserVM
    private lateinit var selectedUserTab : UserTab

    private lateinit var userPostsAdapter : PostItemAdapter
    private var scrollLocked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postsTabVM = ViewModelProviders.of(requireActivity()).get(DisplayUserVM::class.java)

        userPostsAdapter = PostItemAdapter(postsTabVM)

        arguments!!.getParcelable<UserTab>(ARG_USER_TAB)?.let { userTab ->
            selectedUserTab = userTab
        }

        if (checkInternetConnectivity()) {
            postsTabVM.requestPosts(tab = selectedUserTab, refresh = true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user_submissions, container, false).apply {
            userTabRecyclerView.apply {
                adapter = userPostsAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViewModel(viewLifecycleOwner)
        attachScrollListeners()
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        // subscribe to the appropriate livedata based on tab selected
        postsTabVM.getTabPostsLiveData(selectedUserTab).nonNull().observe (lifecycleOwner) {
            setPosts(it)
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

    private fun setPosts(posts : List<ListingItem>) {
//        userPostsAdapter.setPostList(posts)
        userTabSwipeRefreshLayout.isRefreshing = false
        scrollLocked = false
    }

    private fun resetRecyclerView() {
        // empties current items to show that it's being refreshed
        userTabRecyclerView.layoutManager?.scrollToPosition(0)
        userPostsAdapter.clear()

        userTabSwipeRefreshLayout.isRefreshing = true
    }

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