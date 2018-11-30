package com.relic.presentation.home.frontpage

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.frontpage.*

class FrontpageFragment : Fragment() {
    private val TAG = "FRONTPAGE_VIEW"

    private val frontpageVM: FrontpageVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent
                    .builder()
                    .repoModule(RepoModule(context!!))
                    .authModule(AuthModule(context!!))
                    .build()
                    .getDisplayFrontpageVM()
                    .create() as T
            }
        }).get(FrontpageVM::class.java)
    }

    private lateinit var postAdapter: PostItemAdapter
    private lateinit var frontpageRecyclerView : RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frontpage, container, false).apply {
            postAdapter = PostItemAdapter(frontpageVM)

            frontpageRecyclerView = findViewById<RecyclerView>(R.id.frontpagePostsRecyclerView).apply {
                itemAnimator = null
                layoutManager = LinearLayoutManager(context)
                adapter = postAdapter
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frontpageSwipeRefreshLayout.apply {
            setOnRefreshListener {
                // empties current items to show that it's being refreshed
                frontpageRecyclerView.layoutManager!!.scrollToPosition(0)
                postAdapter.clear()

                // tells vm to clear the posts -> triggers action to retrieve more
                frontpageVM.retrieveMorePosts(true)
            }
        }

        bindViewModel()
    }

    private fun bindViewModel() {
        // observe the livedata list of posts for this subreddit
        frontpageVM.postListLiveData.nonNull().observe(this) { postModels ->
            Log.d(TAG, "size of frontpage" + postModels.size)
            postAdapter.setPostList(postModels.toMutableList())

            // unlock scrolling to allow more posts to be loaded
        }
    }
}