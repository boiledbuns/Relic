package com.relic.presentation.displaypost.tabs

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.relic.R
import com.relic.interactor.PostInteractorImpl
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DisplayPostVM
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.tab_fullpost.*
import javax.inject.Inject

class FullPostFragment : RelicFragment() {

    private val fullPostVM by lazy {
        ViewModelProviders.of(requireParentFragment()).get(DisplayPostVM::class.java)
    }

    @Inject
    lateinit var postInteractor: PostInteractorImpl

    private val countDownTimer = object : CountDownTimer(1500, 10000) {
        var active = true

        override fun onFinish() {
            postTabSwipeRefresh.isRefreshing = false
            active = false
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tab_fullpost, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postTabSwipeRefresh.apply {
            isRefreshing = true
            countDownTimer.start()

            setOnRefreshListener {
                fullPostVM.refreshData()
                countDownTimer.start()
            }
        }
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)

        fullPostVM.postLiveData.nonNull().observe(lifecycleOwner) {
            fullPostView.setPost(it)

            // hide refreshing if enough time has passed st there is no jarring animation
            if (!countDownTimer.active) {
                postTabSwipeRefresh.isRefreshing = false
            }
        }

        fullPostView.setViewDelegate(postInteractor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer.cancel()
    }
}