package com.relic.presentation.displaypost.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.DisplayPostVM
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.PostInteractor
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.tab_fullpost.*
import javax.inject.Inject

class FullPostFragment : RelicFragment(), DisplaySubContract.PostViewDelegate{

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

        fullPostView.setViewDelegate(this)
    }

    // region post view delegate

    override fun onPostPressed() = postInteractor.visitPost(getPost())
    override fun onPostSavePressed() = postInteractor.savePost(getPost(), !getPost().saved)
    override fun onPostUpvotePressed() = postInteractor.voteOnPost(getPost(), UPVOTE_PRESSED)
    override fun onPostDownvotePressed() = postInteractor.voteOnPost(getPost(), DOWNVOTE_PRESSED)
    override fun onPostReply() = postInteractor.onNewReplyPressed(getPost())
    override fun onPostLinkPressed() = postInteractor.onLinkPressed(getPost())
    override fun onUserPressed() = postInteractor.previewUser(getPost())

    private fun getPost() = fullPostVM.postLiveData.value!!

    // endregion post view delegate

}