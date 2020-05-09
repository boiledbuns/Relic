package com.relic.presentation.displaysubs

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.relic.R
import com.relic.interactor.Contract
import com.relic.network.NetworkUtil
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.callbacks.AllSubsLoadedCallback
import com.relic.presentation.displaysubs.list.SubItemAdapter
import com.relic.presentation.home.frontpage.FrontpageFragment
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_subs.*
import kotlinx.android.synthetic.main.primary_sources_view.view.*
import java.util.*
import javax.inject.Inject

class DisplaySubsFragment : RelicFragment(), AllSubsLoadedCallback {
    @Inject
    lateinit var factory: DisplaySubsVM.Factory

    @Inject
    lateinit var subredditInteractor: Contract.SubAdapterDelegate

    private val viewModel: DisplaySubsVM by lazy {
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

    private lateinit var subAdapter: SubItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_subs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subAdapter = SubItemAdapter(subredditInteractor)

        display_subs_recyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subAdapter
        }

        default_sources.source_frontpage.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.display_subs_root, FrontpageFragment())
                .addToBackStack(TAG)
                .commit()
        }
        default_sources.source_all.setOnClickListener {
            // TODO when we add the "all" page
        }
        default_sources.source_popular.setOnClickListener {
            // TODO when we add the "popular" page\
        }

        // attach the actions associated with loading the posts
        attachScrollListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)

        searchMenuItem = menu.findItem(R.id.search_item)
        searchView = searchMenuItem.actionView as SearchView

        // initialize a few of the view properties for the searchvie
        val padding = resources.getDimension(R.dimen.search_padding).toInt()
        searchView.setPadding(0, 0, padding, padding)

        // sets the listener for the search item on the menu to capture expand and collapse events
        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                // update visibility for the search recyclerview
//                searchIsVisible = true
//                subscribedListIsVisible = false

                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                // update visibility for the search recyclerview
//                searchIsVisible = false
//                subscribedListIsVisible = true

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
        viewModel.subscribedSubsList.nonNull().observe(lifecycleOwner) {
            display_subs_swiperefreshlayout.isRefreshing = false
            subAdapter.setList(ArrayList(it))
        }

        // observe whether all subscribed subreddits have been loaded
//        viewModel.allSubscribedSubsLoaded.nonNull().observe(this) {
//            if (it) handleOnAllSubsLoaded()
//        }

        viewModel.pinnedSubs.nonNull().observe(lifecycleOwner) { pinnedSubs ->
            pinnedSubsView.setPinnedSubreddits(pinnedSubs)
        }
    }

    override fun handleOnAllSubsLoaded() {
        display_subs_swiperefreshlayout.isRefreshing = false
        display_subs_recyclerview.scrollToPosition(0)
    }

    // endregion view model binding and handlers


    private fun attachScrollListeners() {
        display_subs_swiperefreshlayout.setOnRefreshListener {
//            display_subs_recyclerview.visibility = false

            // refresh the current list and retrieve more posts
            subAdapter.clearList()
            viewModel.refreshSubs()
        }
    }
}
