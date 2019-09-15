package com.relic.presentation.search

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.relic.R
import com.relic.data.PostSource
import com.relic.domain.models.PostModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.NoResults
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.relic.presentation.helper.SearchInputCountdown
import com.relic.presentation.main.RelicError
import kotlinx.android.synthetic.main.display_sub_search.*
import javax.inject.Inject

class PostsSearchFragment : RelicFragment() {
    @Inject
    lateinit var factory : SearchResultsVM.Factory

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    val searchResultsVM: SearchResultsVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create(postSource) as T
            }
        }).get(SearchResultsVM::class.java)
    }

    private lateinit var postSource : PostSource
    private lateinit var postAdapter: PostItemAdapter

    private var scrollLocked = true

    private var countDownTimer : SearchInputCountdown = SearchInputCountdown {
        searchResultsVM.search()
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)
        searchResultsVM.apply {
            postSearchErrorLiveData.nonNull().observe(lifecycleOwner) { handleError(it) }
            postResultsLiveData.nonNull().observe(lifecycleOwner) { handlePostResults(it) }
        }
    }

    private fun handlePostResults(results : List<PostModel>) {
        subSearchResultCount.text = getString(R.string.search_sub_result_count, results.size)
        postAdapter.setPostList(results)

        // hide loading and allow load more again
        scrollLocked = false
        displaySearchProgress.visibility = View.GONE
    }

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postSource = arguments?.getParcelable(ARG_SOURCE) as PostSource
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_sub_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postAdapter = PostItemAdapter(viewPrefsManager, searchResultsVM)
        subSearchRV.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(context)
        }

        initSearchEditText(subSearch)
        subSearch.requestFocus()

        ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                ?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN)

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

    private fun initSearchEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                countDownTimer.cancel()
                countDownTimer.start()

                searchResultsVM.updateQuery(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun handleError(error : RelicError) {
        displaySearchProgress.visibility = View.GONE
        when(error) {
            is NoResults -> {
                Toast.makeText(context, "No more results for query", Toast.LENGTH_SHORT).show()
            }
            is RelicError.NetworkUnavailable -> {
                Snackbar.make(
                        subSearchRoot,
                        resources.getString(R.string.network_unavailable),
                        Snackbar.LENGTH_INDEFINITE
                ).apply {
                    // TODO add functionality for continuing from new search or load more results
//                    setAction(resources.getString(R.string.refresh)) {
//                        searchResultsVM.search(subSearch.text.toString())
//                    }
                    show()
                }
            }
        }
    }

    // endregion lifecycle hooks

    companion object {
        val ARG_SOURCE = "post_source"

        fun create(source  : PostSource) : PostsSearchFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARG_SOURCE, source)

            return PostsSearchFragment().apply {
                arguments = bundle
            }
        }
    }
}