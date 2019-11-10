package com.relic.presentation.search.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.relic.R
import com.relic.data.PostSource
import com.relic.interactor.Contract
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.helper.SearchInputCountdown
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.PostSearchOptions
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_post_search.*
import javax.inject.Inject

class PostSearchFragment : RelicFragment() {
    @Inject
    lateinit var factory : PostSearchResultsVM.Factory

    @Inject
    lateinit var postAdapterDelegate: Contract.PostAdapterDelegate

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    private lateinit var pagerAdapter: PostsSearchPagerAdapter

    val searchResultsVM: PostSearchResultsVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create(postSource) as T
            }
        }).get(PostSearchResultsVM::class.java)
    }

    private lateinit var postSource : PostSource

    private var countDownTimer : SearchInputCountdown = SearchInputCountdown {
        val searchOptions = generateSearchOptions()
        searchResultsVM.search(searchOptions)
    }

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postSource = arguments?.getParcelable(ARG_SOURCE) as PostSource
        pagerAdapter = PostsSearchPagerAdapter().apply {
            fragments.add(PostSearchResultsFragment.create(offline = false))
            fragments.add(PostSearchResultsFragment.create(offline = true))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_post_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postsSearchViewPager.adapter = pagerAdapter
        postsSearchTabLayout.setupWithViewPager(postsSearchViewPager)

        subSearch.initSearchWidget()
    }

    // endregion lifecycle hooks

    private fun SearchView.initSearchWidget() {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                countDownTimer.cancel()
                countDownTimer.start()

                searchResultsVM.updateQuery(newText.toString())

                // action is handled by listener
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                val options = generateSearchOptions()
                searchResultsVM.search(options)

                // action is handled by listener
                return true
            }
        })
    }

    private fun generateSearchOptions() : PostSearchOptions{
        return PostSearchOptions(
            restrictToSource = true
        )
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)
        searchResultsVM.apply {
            postSearchErrorLiveData.observe(lifecycleOwner) { handleError(it) }
        }
    }

    private fun handleError(error : RelicError?) {
        when(error) {
            is RelicError.NetworkUnavailable -> {
                Snackbar.make(
                        postSearchRoot,
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
            null -> {
                // TODO hide error snackbar
            }
        }
    }


    companion object {
        val ARG_SOURCE = "post_source"

        fun create(source  : PostSource) : PostSearchFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARG_SOURCE, source)

            return PostSearchFragment().apply {
                arguments = bundle
            }
        }
    }

    private inner class PostsSearchPagerAdapter : FragmentPagerAdapter(childFragmentManager) {
        val fragments = ArrayList<RelicFragment>()
        val fragmentTitles = listOf("online results", "offline results")

        override fun getCount(): Int = fragments.size

        override fun getItem(p0: Int): Fragment = fragments[p0]
        override fun getPageTitle(position: Int) = fragmentTitles[position]
    }
}