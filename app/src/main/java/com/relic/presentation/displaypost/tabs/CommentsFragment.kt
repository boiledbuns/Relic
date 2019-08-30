package com.relic.presentation.displaypost.tabs

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaypost.DisplayPostVM
import com.relic.presentation.displaypost.list.CommentItemAdapter
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.tab_comments.*

class CommentsFragment : RelicFragment() {

    private val commentsVM by lazy {
        ViewModelProviders.of(parentFragment!!).get(DisplayPostVM::class.java)
    }

    private lateinit var commentAdapter: CommentItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tab_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postCommentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            commentAdapter = CommentItemAdapter(commentsVM)
            adapter = commentAdapter
        }
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)

        commentsVM.commentListLiveData.nonNull().observe(lifecycleOwner) {
            commentAdapter.setComments(it) {
                (parentFragment as DisplayPostFragment).onPostDataLoaded()
            }
        }
    }
}