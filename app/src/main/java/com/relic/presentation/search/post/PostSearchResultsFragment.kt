package com.relic.presentation.search.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.interactor.Contract
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.NoResults
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.relic.presentation.main.MainActivity
import com.relic.presentation.main.RelicError
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.post_results.*
import javax.inject.Inject

class PostSearchResultsFragment : RelicFragment() {
    @Inject
    lateinit var postInteractor : Contract.PostAdapterDelegate

    private val searchResultsVM by lazy {
        ViewModelProviders.of(parentFragment!!).get(PostSearchResultsVM::class.java)
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

        postAdapter = PostItemAdapter(viewPrefsManager, postInteractor) {}

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
            postSearchErrorLiveData.observe(lifecycleOwner) { handleError(it) }

            if (offline) {
                offlinePostResultsLiveData.nonNull().observe(lifecycleOwner) { handlePostResults(it) }
            } else {
                postResultsLiveData.nonNull().observe(lifecycleOwner) { handlePostResults(it) }
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

    private fun handleError(error : RelicError?) {
        displaySearchProgress.visibility = View.GONE
        when(error) {
            is NoResults -> {
                Toast.makeText(context, "No more results for query", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        val ARG_OFFLINE_RESULTS = "offline_results"

        fun create(offline : Boolean) : PostSearchResultsFragment {
            val bundle = Bundle()
            bundle.putBoolean(ARG_OFFLINE_RESULTS, offline)

            return PostSearchResultsFragment().apply {
                arguments = bundle
            }
        }
    }
}