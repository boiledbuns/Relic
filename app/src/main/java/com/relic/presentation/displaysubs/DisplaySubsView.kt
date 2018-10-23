package com.relic.presentation.displaysubs

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.data.models.SubredditModel
import com.relic.databinding.DisplaySubsBinding
import com.relic.presentation.adapter.SearchItemAdapter
import com.relic.presentation.adapter.SearchSubItemOnClick
import com.relic.presentation.adapter.SubItemAdapter
import com.relic.presentation.adapter.SubItemOnClick
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.callbacks.AllSubsLoadedCallback
import com.relic.presentation.displaysub.DisplaySubView
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import com.relic.presentation.subinfodialog.SubInfoDialogContract.Companion.ARG_SUB_NAME
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe

import java.util.ArrayList

class DisplaySubsView : RelicFragment(), AllSubsLoadedCallback {
    private val viewModel : DisplaySubsVM by lazy {
        ViewModelProviders.of(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent.builder()
                        .authModule(AuthModule(activity!!.applicationContext))
                        .repoModule(RepoModule(activity!!.applicationContext))
                        .build().getDisplaySubsVM().create() as T
            }
        }).get(DisplaySubsVM::class.java)
    }

    private lateinit var searchView: SearchView
    private lateinit var searchMenuItem: MenuItem

    private lateinit var displaySubsBinding: DisplaySubsBinding
    private lateinit var subAdapter: SubItemAdapter
    private lateinit var searchItemAdapter: SearchItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindViewModel()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // inflate the databinding view
        displaySubsBinding = DataBindingUtil
                .inflate(inflater, R.layout.display_subs, container, false)

        displaySubsBinding.apply {
            // initialize the adapter for the search subs recyclerview
            val searchSubItemOnClick = OnClickSearchSubItem(this@DisplaySubsView)
            subAdapter = SubItemAdapter(OnClickSubItem())
            searchItemAdapter = SearchItemAdapter(searchSubItemOnClick)

            displaySubsRecyclerview.also {
                it.layoutManager = GridLayoutManager(context, 3)
                it.itemAnimator = null
                it.adapter = subAdapter
            }

            searchSubsRecyclerview.apply {
                adapter = searchItemAdapter
                layoutManager = LinearLayoutManager(context)
            }

            (displaySubsToolbar as Toolbar).also {
                it.setTitle(R.string.app_name)
                (activity as AppCompatActivity).setSupportActionBar(it)
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

        // inialize a few of the view properties for the searchvie
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

    // region viewmodel binding and handlers

    private fun bindViewModel() {
        // allows the list to be updated as subreddits are retrieved from the network
        viewModel.subscribedSubsList.nonNull().observe(this) {
            subAdapter.setList(ArrayList(it))
        }

        // observe whether all subscribed subreddits have been loaded
        viewModel.allSubscribedSubsLoaded.nonNull().observe(this) {
            if (it) handleOnAllSubsLoaded()
        }

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

    // end region viewmodel binding and handlers


    fun attachScrollListeners() {
        displaySubsBinding.apply {
            displaySubsSwiperefreshlayout.setOnRefreshListener {
                displaySubsBinding.subscribedListIsVisible = false

                // refresh the current list and retrieve more posts
                subAdapter.clearList()
                viewModel.retrieveMoreSubs(true)
            }
        }
    }

    /**
     * onClick class for the xml file to hook to
     */
    internal inner class OnClickSubItem : SubItemOnClick {
        override fun onClick(subItem: SubredditModel) {
            val subFrag = DisplaySubView()

            // add the subreddit object to the bundle
            val bundle = Bundle().apply {
                putString("SubredditName", subItem.subName)
                subFrag.arguments = this
            }

            // clear items before transition to ensure we don't hold too much in memory
            subAdapter.clearList()

            // TODO : find a way to stop recreating the fragment everytime and keep the position in the list
            // this applies to sub view as well
            transitionToFragment(subFrag)
        }

        override fun onLongClick(subItem: SubredditModel): Boolean {
            val args = Bundle().apply {
                putString(ARG_SUB_NAME, subItem.name)
            }

            val newDialog = SubInfoBottomSheetDialog()
            newDialog.arguments= args
            newDialog.show(fragmentManager, TAG)

            return true
        }
    }

    internal inner class OnClickSearchSubItem(fragment: Fragment) : SearchSubItemOnClick {
        override fun onClick(subName: String) {

            // add the subreddit object to the bundle
            val bundle = Bundle()
            bundle.putString("SubredditName", subName)

            val subFrag = DisplaySubView()
            subFrag.arguments = bundle

            // clear items in the adapter
            searchItemAdapter.clearSearchResults()
        }
    }
}
