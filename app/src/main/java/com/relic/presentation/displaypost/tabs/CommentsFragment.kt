package com.relic.presentation.displaypost.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.relic.R
import com.relic.interactor.CommentInteractorImpl
import com.relic.interactor.PostInteractorImpl
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DisplayPostVM
import com.relic.presentation.displaypost.comments.CommentItemAdapter
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.tab_comments.*
import javax.inject.Inject

class CommentsFragment : RelicFragment() {

    @Inject
    lateinit var postInteractor: PostInteractorImpl

    @Inject
    lateinit var commentInteractor: CommentInteractorImpl

    private val commentsVM by lazy {
        ViewModelProviders.of(requireParentFragment()).get(DisplayPostVM::class.java)
    }

    private lateinit var commentAdapter: CommentItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tab_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postCommentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            commentAdapter = CommentItemAdapter(commentsVM, commentInteractor, postInteractor)
            adapter = commentAdapter
        }

        commentsTabSwipeRefresh.apply {
            isRefreshing = true
            setOnRefreshListener {
                commentsVM.refreshData()
            }
        }
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)

        commentsVM.commentListLiveData.nonNull().observe(lifecycleOwner) {
            commentAdapter.setComments(it) {}

            commentsTabSwipeRefresh.isRefreshing = false
        }
    }
}