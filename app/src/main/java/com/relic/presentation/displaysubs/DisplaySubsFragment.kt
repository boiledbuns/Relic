package com.relic.presentation.displaysubs

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.relic.R
import com.relic.domain.models.SubredditModel
import com.relic.databinding.DisplaySubsBinding
import com.relic.network.NetworkUtil
import com.relic.presentation.adapter.SearchItemAdapter
import com.relic.presentation.adapter.SearchSubItemOnClick
import com.relic.presentation.adapter.SubItemAdapter
import com.relic.presentation.adapter.SubItemOnClick
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.callbacks.AllSubsLoadedCallback
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import com.relic.presentation.subinfodialog.SubInfoDialogContract.Companion.ARG_SUB_NAME
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_subs.*

import java.util.ArrayList
import javax.inject.Inject

class DisplaySubsFragment : RelicFragment(), AllSubsLoadedCallback {
    @Inject
    lateinit var factory : DisplaySubsVM.Factory

    private val viewModel : DisplaySubsVM by lazy {
        ViewModelProviders.of(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create() as T
            }
        }).get(DisplaySubsVM::class.java)
    }

    @Inject
    lateinit var networkUtil: NetworkUtil

    private lateinit var searchView: SearchView
    private lateinit var searchMenuItem: MenuItem

    private lateinit var displaySubsBinding: DisplaySubsBinding
    private lateinit var subAdapter: SubItemAdapter
    private lateinit var searchItemAdapter: SearchItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        subAdapter = SubItemAdapter(OnClickSubItem())

        // initialize the adapter for the search subs recycler view
        val searchSubItemOnClick = OnClickSearchSubItem(this)
        searchItemAdapter = SearchItemAdapter(searchSubItemOnClick)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // inflate the data binding view
        displaySubsBinding = DataBindingUtil
                .inflate(inflater, R.layout.display_subs, container, false)

        displaySubsBinding.apply {

            displaySubsRecyclerview.also {
                it.layoutManager = GridLayoutManager(context, 3)
                it.adapter = subAdapter
            }

            searchSubsRecyclerview.apply {
                adapter = searchItemAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }

        // attach the actions associated with loading the posts
        attachScrollListeners()
        return displaySubsBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.search_menu, menu)

        searchMenuItem = menu!!.findItem(R.id.search_item)
        searchView = searchMenuItem.actionView as SearchView

        // initialize a few of the view properties for the searchvie
        val padding = resources.getDimension(R.dimen.search_padding).toInt()
        searchView.setPadding(0, 0, padding, padding)

        // sets the listener for the search item on the menu to capture expand and collapse events
        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                // update visibility for the search recyclerview
                displaySubsBinding.apply {
                    searchIsVisible = true
                    subscribedListIsVisible = false
                }
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                // update visibility for the search recyclerview
                displaySubsBinding.apply {
                    searchIsVisible = false
                    subscribedListIsVisible = true
                }

                // clear items in the adapter
                searchItemAdapter.clearSearchResults()
                return true
            }
        })

        // add query listeners to the searchview
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.retrieveSearchResults(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // sends search query to viewmodel, use submit because we search whenever query is changed
                return onQueryTextSubmit(query)
            }
        })
    }

    // region view model binding and handlers

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        // allows the list to be updated as subreddits are retrieved from the network
        viewModel.subscribedSubsList.nonNull().observe(this) {
            displaySubsBinding.displaySubsSwiperefreshlayout.isRefreshing = false
            subAdapter.setList(ArrayList(it))
        }

        // observe whether all subscribed subreddits have been loaded
//        viewModel.allSubscribedSubsLoaded.nonNull().observe(this) {
//            if (it) handleOnAllSubsLoaded()
//        }

        viewModel.searchResults.nonNull().observe(this) { results ->
            searchItemAdapter.handleSearchResultsPayload(results)
        }

        viewModel.pinnedSubs.nonNull().observe(this) { pinnedSubs ->
            displaySubsBinding.pinnedSubsView.setPinnedSubreddits(pinnedSubs)
        }
    }

    override fun handleOnAllSubsLoaded() {
        displaySubsBinding.apply {
            displaySubsSwiperefreshlayout.isRefreshing = false
            displaySubsRecyclerview.scrollToPosition(0)
        }
    }

    // endregion view model binding and handlers


    private fun attachScrollListeners() {
        displaySubsBinding.apply {
            displaySubsSwiperefreshlayout.setOnRefreshListener {
                displaySubsBinding.subscribedListIsVisible = false

                // refresh the current list and retrieve more posts
                subAdapter.clearList()
                viewModel.refreshSubs()
            }
        }
    }

    /**
     * onClick class for the xml file to hook to
     */
    internal inner class OnClickSubItem : SubItemOnClick {
        override fun onClick(subItem: SubredditModel) {
            val subFrag = DisplaySubFragment.create(subItem.subName)

            // clear items before transition to ensure we don't hold too much in memory
            subAdapter.clearList()

            // TODO : find a way to stop recreating the fragment everytime and keep the position in the list
            // this applies to sub view as well
            transitionToFragment(subFrag)
        }

        override fun onLongClick(subItem: SubredditModel): Boolean {
            val args = Bundle().apply {
                putString(ARG_SUB_NAME, subItem.subName)
            }

            val newDialog = SubInfoBottomSheetDialog()
            newDialog.arguments= args
            newDialog.show(fragmentManager, TAG)

            return true
        }
    }

    internal inner class OnClickSearchSubItem(fragment: Fragment) : SearchSubItemOnClick {
        override fun onClick(subName: String) {

            val subFrag = DisplaySubFragment.create(subName)
            // clear items in the adapter
            searchItemAdapter.clearSearchResults()
        }
    }
}
