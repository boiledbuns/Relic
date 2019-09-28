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
import com.relic.data.PostSource
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.helper.SearchInputCountdown
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.SubredditSearchOptions
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
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

    private val countDownTimer : SearchInputCountdown by lazy {
        SearchInputCountdown {
            val searchOptions = generateSearchOptions()
            subSearchVM.search(searchOptions)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_sub_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        offlineResultsAdapter = SearchSubItemAdapter(subSearchVM)
        searchResultsAdapter = SearchSubPreviewItemAdapter(subSearchVM)

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
                clearFocus()
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
            navigationLiveData.nonNull().observe(lifecycleOwner) { handleNavigation(it) }
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

    private fun handleNavigation(navigationData: NavigationData) {
        when(navigationData) {
            is NavigationData.ToPostSource -> {
                if (navigationData.source is PostSource.Subreddit) {
                    val subFrag = DisplaySubFragment.create(navigationData.source.subredditName)
                    activity!!.supportFragmentManager
                            .beginTransaction()
                            .add(R.id.main_content_frame, subFrag)
                            .addToBackStack(TAG)
                            .commit()
                }
            }
            is NavigationData.PreviewPostSource -> {
                if (navigationData.source is PostSource.Subreddit) {
                   SubInfoBottomSheetDialog.create(navigationData.source.subredditName)
                           .show(activity!!.supportFragmentManager, TAG)
                }
            }
        }
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