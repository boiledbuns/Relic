package com.relic.presentation.search.subreddit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.relic.R
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.helper.SearchInputCountdown
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.SubredditSearchOptions
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_sub_search.*
import javax.inject.Inject

class SubSearchFragment : RelicFragment() {
    @Inject
    lateinit var factory : SubSearchVM.Factory

    private val subSearchVM: SubSearchVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create() as T
            }
        }).get(SubSearchVM::class.java)
    }

    private lateinit var offlineResultsAdapter : SearchSubItemAdapter
    private lateinit var searchResultsAdapter : SearchSubPreviewItemAdapter

    private var countDownTimer : SearchInputCountdown = SearchInputCountdown {
        val searchOptions = generateSearchOptions()
        subSearchVM.search(searchOptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offlineResultsAdapter = SearchSubItemAdapter()
        searchResultsAdapter = SearchSubPreviewItemAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_sub_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subSearch.initSearchWidget()

        localSubResultsRV.apply {
            adapter = offlineResultsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        searchSubResultsRV.apply {
            adapter = searchResultsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun SearchView.initSearchWidget() {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                countDownTimer.cancel()
                countDownTimer.start()
                subSearchVM.updateQuery(newText.toString())

                val options = generateSearchOptions()
                subSearchVM.search(options)

                // action is handled by listener
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                // action is handled by listener
                return true
            }
        })
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)
        subSearchVM.apply {
            subredditResultsLiveData.nonNull().observe(lifecycleOwner) { handleSearchResults(it) }
            subscribedSubredditResultsLiveData.nonNull().observe(lifecycleOwner) { handleLocalSearchResults(it) }
            subSearchErrorLiveData.nonNull().observe(lifecycleOwner) { handleError(it) }
        }
    }

    private fun handleSearchResults(subreddits : List<SubPreviewModel>) {
        searchResultsAdapter.updateSearchResults(subreddits)
        onlineSubsResultSize.text = getString(R.string.sub_search_results_size, subreddits.size)
    }

    private fun handleLocalSearchResults(subreddits : List<SubredditModel>) {
        offlineResultsAdapter.updateSearchResults(subreddits)
        localSubsResultSize.text = getString(R.string.sub_search_results_size, subreddits.size)
    }

    private fun handleError(error : RelicError?) {
        if (error == null) {
            // hide the toast
        } else {
            Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateSearchOptions() : SubredditSearchOptions{
        return SubredditSearchOptions()
    }

    companion object {
        fun create() : SubSearchFragment {
            return SubSearchFragment()
        }
    }

}