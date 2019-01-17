package com.relic.presentation.displayuser.fragments

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.relic.presentation.displayuser.DisplayUserVM
import com.relic.presentation.displayuser.UserTab
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user_submissions.view.*

class PostsTabFragment : RelicFragment() {

    private lateinit var postsTabVM : DisplayUserVM
    private lateinit var selectedUserTab : UserTab

    private lateinit var userPostsAdapter : PostItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postsTabVM = ViewModelProviders.of(requireActivity()).get(DisplayUserVM::class.java)
        userPostsAdapter = PostItemAdapter(postsTabVM)

        arguments!!.getParcelable<UserTab>(ARG_USER_TAB)?.let { userTab ->
            selectedUserTab = userTab
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
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
         // subscribe to the appropriate livedata based on tab selected
        when(selectedUserTab) {
            is UserTab.Submissions -> postsTabVM.submissionLiveData.nonNull().observe (lifecycleOwner) {
                userPostsAdapter.setPostList(it)
            }
            is UserTab.Comments -> {}
            is UserTab.Saved -> {}
            is UserTab.Upvoted -> {}
            is UserTab.Downvoted -> {}
            is UserTab.Gilded -> {}
            is UserTab.Hidden -> {}
        }
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