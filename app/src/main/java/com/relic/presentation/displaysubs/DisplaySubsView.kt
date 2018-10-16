package com.relic.presentation.displaysubs

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

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
import com.relic.presentation.callbacks.AllSubsLoadedCallback
import com.relic.presentation.displaysub.DisplaySubView
import com.relic.presentation.subinfodialog.SubInfoBottomSheetDialog
import com.relic.presentation.subinfodialog.SubInfoDialogContract.Companion.ARG_SUB_NAME
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_subs.*

import java.util.ArrayList

class DisplaySubsView : Fragment(), AllSubsLoadedCallback {
    private val TAG = "DISPLAY_SUBS_VIEW"

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

    private var searchView: SearchView? = null
    private var searchMenuItem: MenuItem? = null

    private var displaySubsBinding: DisplaySubsBinding? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var myToolbar: Toolbar? = null
    private var subAdapter: SubItemAdapter? = null
    private var searchItemAdapter: SearchItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // inflate the databinding view
        displaySubsBinding = DataBindingUtil
                .inflate(inflater, R.layout.display_subs, container, false)

        // initialize the adapter for subscribed subs recyclerview
        subAdapter = SubItemAdapter(OnClickSubItem())
        displaySubsBinding!!.displaySubsRecyclerview.itemAnimator = null
        displaySubsBinding!!.displaySubsRecyclerview.adapter = subAdapter
        // displays the subscribed subs in 3 columns
        val gridLayoutManager = GridLayoutManager(context, 3)
        displaySubsBinding!!.displaySubsRecyclerview.layoutManager = gridLayoutManager

        // initialize the adapter for the search subs recyclerview
        val searchSubItemOnClick = OnClickSearchSubItem(this)
        searchItemAdapter = SearchItemAdapter(searchSubItemOnClick)
        displaySubsBinding!!.searchSubsRecyclerview.adapter = searchItemAdapter
        displaySubsBinding!!.searchSubsRecyclerview.layoutManager = LinearLayoutManager(context)

        // sets defaults for the actionbar
        myToolbar = displaySubsBinding!!.root.findViewById(R.id.display_subs_toolbar)
        (myToolbar!!.findViewById<View>(R.id.my_toolbar_title) as TextView).text =
                getText(R.string.app_name)

        val parentActivity = activity as AppCompatActivity?
        parentActivity?.setSupportActionBar(myToolbar)

        // attach the actions associated with loading the posts
        attachScrollListeners()
        initializeLivedata()

        return displaySubsBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.search_menu, menu)

        searchMenuItem = menu!!.findItem(R.id.search_item)
        searchView = searchMenuItem!!.actionView as SearchView

        // inialize a few of the view properties for the searchvie
        val padding = resources.getDimension(R.dimen.search_padding).toInt()
        searchView!!.setPadding(0, 0, padding, padding)

        // sets the listener for the search item on the menu to capture expand and collapse events
        searchMenuItem!!.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                // update visibility for the search recyclerview
                displaySubsBinding!!.searchIsVisible = true
                displaySubsBinding!!.subscribedListIsVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                // update visibility for the search recyclerview
                displaySubsBinding!!.searchIsVisible = false
                displaySubsBinding!!.subscribedListIsVisible = true

                // clear items in the adapter
                searchItemAdapter!!.clearSearchResults()
                return true
            }
        })

        // Add query listeners to the searchview
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    /**
     * Subscribes to various livedata values from the viewmodel
     */
    private fun bindViewModel() {
        // allows the list to be updated as data is updated
        viewModel.subscribedList.nonNull().observe(this) { subredditsList: List<SubredditModel> ->
            // updates the view once the list is loaded
            if (!subredditsList.isEmpty()) {
                subAdapter!!.setList(ArrayList(subredditsList))
                //Log.d(TAG, "Changes to subreddit list received $subredditsList")
            }
        }

        // observe whether all subscribed subreddits have been loaded
        viewModel.allSubscribedSubsLoaded.nonNull().observe(this) { completelyLoaded: Boolean ->
            //Log.d(TAG, "Refreshing status changed to $completelyLoaded")
            if (completelyLoaded) {
                swipeRefreshLayout!!.isRefreshing = false
                displaySubsBinding!!.displaySubsRecyclerview.scrollToPosition(0)
                displaySubsBinding!!.subscribedListIsVisible = true
            }
        }

        // subscribes to search results
        viewModel.searchResults.observe(this, Observer { results ->
            // update the view based on search results
            if (results != null) {
                //Toast.makeText(getContext(), " " + results.toString(), Toast.LENGTH_SHORT).show();
                //Log.d(TAG, " " + results.toString())
                searchItemAdapter!!.setSearchResults(results)
            }
        })

        viewModel.pinnedSubs.nonNull().observe { pinnedSubs ->
            Log.d(TAG, " pinned subs " + pinnedSubs.size)
            displaySubsBinding?.pinnedSubsView?.setPinnedSubreddits(pinnedSubs)
        }
    }

    private fun initializeLivedata() {
        //    // initialize livedata properties for binding
        //    searchIsVisible = new MutableLiveData<>();
        //    // initialize observer for updating binding on change
        //    searchIsVisible.observe(this, (Boolean isVisible) -> {
        //      displaySubsBinding.setSearchIsVisible(isVisible);
        //    });
        //    searchIsVisible.setValue(false);
    }

    fun attachScrollListeners() {
        swipeRefreshLayout =
                displaySubsBinding!!.root.findViewById(R.id.display_subs_swiperefreshlayout)
        swipeRefreshLayout!!.setOnRefreshListener {
            displaySubsBinding!!.subscribedListIsVisible = false

            // refresh the current list and retrieve more posts
            subAdapter!!.resetSubList()
            viewModel.retrieveMoreSubs(true)
        }
    }

    override fun onAllSubsLoaded() {
        //swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * onClick class for the xml file to hook to
     */
    internal inner class OnClickSubItem : SubItemOnClick {
        override fun onClick(subItem: SubredditModel) {
            Log.d(TAG, subItem.subName)

            // add the subreddit object to the bundle
            val bundle = Bundle()
            bundle.putString("SubredditName", subItem.subName)

            val subFrag = DisplaySubView()
            subFrag.arguments = bundle

            // replace the current screen with the newly created fragment
            activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit()
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
        val owner: LifecycleOwner = this@DisplaySubsView

        override fun onClick(subName: String) {
            Log.d(TAG, "Search item clicked")

            // add the subreddit object to the bundle
            val bundle = Bundle()
            bundle.putString("SubredditName", subName)

            val subFrag = DisplaySubView()
            subFrag.arguments = bundle

            // clear items in the adapter
            searchItemAdapter!!.clearSearchResults()

            // replace the current screen with the newly created fragment
            activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit()
        }
    }
}
