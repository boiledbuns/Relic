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
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.UserModel
import com.relic.interactor.Contract
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.helper.SearchInputCountdown
import com.relic.presentation.main.RelicError
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
    lateinit var subInteractor: Contract.SubAdapterDelegate
    @Inject
    lateinit var postInteractor: Contract.PostAdapterDelegate
    @Inject
    lateinit var userInteractor: Contract.UserAdapterDelegate

    @Inject
    lateinit var viewPrefsManager: ViewPreferencesManager

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

        searchResultsAdapter = SearchResultsAdapter(SearchResultType.SUB, subInteractor, postInteractor, userInteractor, viewPrefsManager)
        searchResultsRV.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultsAdapter
        }

        // TODO convert interactors
        // setup the direct navigation cards
        directToSub.setOnClickListener { }
        directToUser.setOnClickListener { }
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

            subSearchErrorLiveData.nonNull().observe(lifecycleOwner) { handleError(it) }
            loadingLiveData.nonNull().observe(lifecycleOwner) { handleLoading(it) }
        }
    }

    override fun handleNavReselected(): Boolean {
        return true
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
                    searchVM.search(newText, generateSearchOptions())
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
                optionToResultTypeMap[checkedId]?.let { resultType ->
                    searchVM.changeSearchResultType(resultType, generateSearchOptions())
                    searchResultsAdapter.setResultType(resultType)
                }
            }
        }
    }

    private fun generateSearchOptions(): SubredditSearchOptions {
        return SubredditSearchOptions()
    }
}