package com.relic.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.relic.R
import com.relic.data.PostSource
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.UserModel
import com.relic.interactor.Contract
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.helper.SearchInputCountdown
import com.relic.presentation.main.RelicError
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import com.shopify.livedataktx.nonNull
import kotlinx.android.synthetic.main.display_search.*
import javax.inject.Inject


class SearchFragment : RelicFragment() {
    @Inject
    lateinit var factory: SearchVM.Factory

    private val searchVM: SearchVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create() as T
            }
        }).get(SearchVM::class.java)
    }

    @Inject
    lateinit var viewPrefsManager: ViewPreferencesManager

    @Inject
    lateinit var postInteractor: Contract.PostAdapterDelegate

    private val countDownTimer = SearchInputCountdown()

    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private val optionToResultTypeMap = mapOf(
        Pair(R.id.optionSubs, SearchResultType.SUB),
        Pair(R.id.optionPosts, SearchResultType.POST),
        Pair(R.id.optionUsers, SearchResultType.USER)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchResultsAdapter = SearchResultsAdapter(ResultType.SUB, searchVM, postInteractor, viewPrefsManager)
        searchResultsRV.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultsAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // initialize listeners AFTER state has been restored
        searchView.initSearchWidget()
        initializeSearchOptions(searchVM.currentSearchType)
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        searchVM.apply {
            subredditResultsLiveData.observe(lifecycleOwner) { handleSearchResults(subs = it) }
            postResultsLiveData.observe(lifecycleOwner) { handleSearchResults(posts = it) }
            userResultsLiveData.observe(lifecycleOwner) { handleSearchResults(users = it) }

            navigationLiveData.nonNull().observe(lifecycleOwner) { handleNavigation(it) }
            subSearchErrorLiveData.nonNull().observe(lifecycleOwner) { handleError(it) }
            loadingLiveData.nonNull().observe(lifecycleOwner) { handleLoading(it) }
        }
    }

    // region livedata handlers
    private fun handleSearchResults(
        subs: List<SubPreviewModel>? = null,
        posts: List<PostModel>? = null,
        users: List<UserModel>? = null
    ) {
        subs?.let { searchResultsAdapter.updateSearchResults(newSubSearchResults = it) }
        posts?.let { searchResultsAdapter.updateSearchResults(newPostSearchResults = it) }
        users?.let { searchResultsAdapter.updateSearchResults(newUserSearchResults = it) }
        //        onlineSubsResultSize.text = getString(R.string.sub_search_results_size, subreddits.size)
    }

    private fun handleNavigation(navigationData: NavigationData) {
        when (navigationData) {
            is NavigationData.ToPostSource -> {
                if (navigationData.source is PostSource.Subreddit) {
                    val subFrag = DisplaySubFragment.create(navigationData.source.subredditName)
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .add(R.id.main_content_frame, subFrag)
                        .addToBackStack(TAG)
                        .commit()
                }
            }
            is NavigationData.PreviewPostSource -> {
                if (navigationData.source is PostSource.Subreddit) {
                    SubInfoBottomSheetDialog.create(navigationData.source.subredditName)
                        .show(requireActivity().supportFragmentManager, TAG)
                }
            }
        }
    }

    private fun handleError(error: RelicError?) {
        if (error == null) {
            // hide the toast
        } else {
            Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLoading(loading: Boolean) {
        if (loading) {
            searchProgress.visibility = View.VISIBLE
            searchResultsAdapter.clear()
        } else {
            searchProgress.visibility = View.INVISIBLE
        }
    }
    // endregion livedata handlers

    private fun SearchView.initSearchWidget() {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                // cancel previous timer and start new one for new texts
                countDownTimer.cancel()
                countDownTimer.start {
                    searchVM.updateQuery(newText)
                    searchVM.search(generateSearchOptions())
                }

                renderCard(newText)
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

    // sets up rendering for the direct navigation card
    private fun renderCard(newText: String?) {
        if (newText.isNullOrEmpty()) {
            directNavigationCard.visibility = View.GONE
        } else {
            directToSub.text = "r/$newText"
            directToUser.text = "u/$newText"
            directNavigationCard.visibility = View.VISIBLE
        }
    }

    // sets up the onclicks for search options
    private fun initializeSearchOptions(selectedType: SearchResultType) {
        searchOptions.apply {
            val initialIdMap = optionToResultTypeMap.filter { pair -> pair.value == selectedType }
            check(initialIdMap.keys.first())

            setOnCheckedChangeListener { _, checkedId ->
                optionToResultTypeMap[checkedId]?.let {
                    searchVM.changeSearchResultType(it, generateSearchOptions())
                }
            }
        }
    }

    private fun generateSearchOptions(): SubredditSearchOptions {
        return SubredditSearchOptions()
    }
}