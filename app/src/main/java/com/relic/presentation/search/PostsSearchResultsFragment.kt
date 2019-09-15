package com.relic.presentation.search

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.NoResults
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.relic.presentation.main.MainActivity
import com.relic.presentation.main.RelicError
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.post_results.*

class PostsSearchResultsFragment : RelicFragment() {
    private val searchResultsVM by lazy {
        ViewModelProviders.of(parentFragment!!).get(SearchResultsVM::class.java)
    }

    private val viewPrefsManager by lazy {
        (activity as MainActivity).viewPrefsManager
    }
    private lateinit var postAdapter: PostItemAdapter

    private var offline: Boolean = false
    private var scrollLocked = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offline = arguments?.getBoolean(ARG_OFFLINE_RESULTS, false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.post_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postAdapter = PostItemAdapter(viewPrefsManager, searchResultsVM)
        subSearchRV.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(context)
        }

        attachScrollListeners()
    }

    private fun attachScrollListeners() {
        subSearchRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // checks if the recycler view can no longer scroll downwards
                if (!recyclerView.canScrollVertically(1) && !scrollLocked) {
                    // lock scrolling until set of posts are loaded to prevent additional unwanted retrievals
                    scrollLocked = true
                    displaySearchProgress.visibility = View.VISIBLE

                    // fetch the next post listing
                    searchResultsVM.retrieveMorePostResults()
                }
            }
        })
    }


    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)

        searchResultsVM.apply {
            if (offline) {
                postSearchErrorLiveData.nonNull().observe(lifecycleOwner) { handleError(it) }
                postResultsLiveData.nonNull().observe(lifecycleOwner) { handlePostResults(it) }
            } else {
                // TODO add offline
            }
        }
    }

    private fun handlePostResults(results : List<PostModel>) {
        subSearchResultCount.text = getString(R.string.search_sub_result_count, results.size)
        postAdapter.setPostList(results)

        // hide loading and allow load more again
        scrollLocked = false
        displaySearchProgress.visibility = View.GONE
    }

    private fun handleError(error : RelicError) {
        displaySearchProgress.visibility = View.GONE
        when(error) {
            is NoResults -> {
                Toast.makeText(context, "No more results for query", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        val ARG_OFFLINE_RESULTS = "offline_results"

        fun create(offline : Boolean) : PostsSearchResultsFragment {
            val bundle = Bundle()
            bundle.putBoolean(ARG_OFFLINE_RESULTS, offline)

            return PostsSearchResultsFragment().apply {
                arguments = bundle
            }
        }
    }
}