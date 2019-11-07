package com.relic.presentation.displaypost.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DisplayPostVM
import com.relic.presentation.displaysub.PostInteractor
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.tab_fullpost.*
import javax.inject.Inject

class FullPostFragment : RelicFragment() {

    private val fullPostVM by lazy {
        ViewModelProviders.of(parentFragment!!).get(DisplayPostVM::class.java)
    }

    @Inject
    lateinit var postInteractor: PostInteractor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tab_fullpost, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postTabSwipeRefresh.apply{
            isRefreshing = true

            setOnRefreshListener {
                fullPostVM.refreshData()
            }
        }
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)

        fullPostVM.postLiveData.nonNull().observe(lifecycleOwner) {
            fullPostView.setPost(it)
            postTabSwipeRefresh.isRefreshing = false
        }

        fullPostView.setViewDelegate(postInteractor)
    }
}