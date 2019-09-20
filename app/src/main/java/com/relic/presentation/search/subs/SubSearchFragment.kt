package com.relic.presentation.search.subs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.relic.R
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

    lateinit var offlineResultsAdapter : SearchSubNameItemAdapter
    lateinit var searchResultsAdapter : SearchSubItemAdapter

    private var countDownTimer : SearchInputCountdown = SearchInputCountdown {
        val searchOptions = generateSearchOptions()
        subSearchVM.search(searchOptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offlineResultsAdapter = SearchSubNameItemAdapter()
        searchResultsAdapter = SearchSubItemAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_sub_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subSearch.initSearchWidget()
    }

    private fun SearchView.initSearchWidget() {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                countDownTimer.cancel()
                countDownTimer.start()

                subSearchVM.updateQuery(newText.toString())

                // action is handled by listener
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                val options = generateSearchOptions()
                subSearchVM.search(options)

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

    private fun handleSearchResults(subreddits : List<String>) {

    }

    private fun handleLocalSearchResults(subreddits : List<SubredditModel>) {

    }

    private fun handleError(error : RelicError?) {

    }


    private fun generateSearchOptions() : SubredditSearchOptions{
        return SubredditSearchOptions()
    }

}