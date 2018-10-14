package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.relic.R;
import com.relic.data.Authenticator;
import com.relic.data.ListingRepositoryImpl;
import com.relic.data.SubRepositoryImpl;
import com.relic.data.models.SubredditModel;
import com.relic.databinding.DisplaySubsBinding;
import com.relic.presentation.adapter.SearchItemAdapter;
import com.relic.presentation.adapter.SearchSubItemOnClick;
import com.relic.presentation.adapter.SubItemAdapter;
import com.relic.presentation.adapter.SubItemOnClick;
import com.relic.presentation.callbacks.AllSubsLoadedCallback;
import com.relic.presentation.displaysub.DisplaySubView;

import java.util.ArrayList;
import java.util.List;

public class DisplaySubsView extends Fragment implements AllSubsLoadedCallback{
  private final String TAG = "DISPLAY_SUBS_VIEW";
  DisplaySubsContract.VM viewModel;

  private SearchView searchView;
  private MenuItem searchMenuItem;

  private DisplaySubsBinding displaySubsBinding;
  private SwipeRefreshLayout swipeRefreshLayout;

  private Toolbar myToolbar;
  private SubItemAdapter subAdapter;
  private SearchItemAdapter searchItemAdapter;

  private MutableLiveData<Boolean> searchIsVisible;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    if (getActivity() != null) {
      // retrieve the instance of the viewmodel and attach a reference to it in this view
      viewModel = ViewModelProviders.of(getActivity()).get(DisplaySubsVM.class);

      // initialize the repository and inject it into the viewmodel
      viewModel.init(new SubRepositoryImpl(getContext()), new ListingRepositoryImpl(getContext()),
          new Authenticator(getContext()));
    }
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // inflate the databinding view
    displaySubsBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_subs, container, false);

    // initialize the adapter for subscribed subs recyclerview
    subAdapter = new SubItemAdapter(new OnClickSubItem());
    displaySubsBinding.displaySubsRecyclerview.setItemAnimator(null );
    displaySubsBinding.displaySubsRecyclerview.setAdapter(subAdapter);
    // displays the subscribed subs in 3 columns
    GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
    displaySubsBinding.displaySubsRecyclerview.setLayoutManager(gridLayoutManager);

    // initialize the adapter for the search subs recyclerview
    SearchSubItemOnClick searchSubItemOnClick = new OnClickSearchSubItem(this);
    searchItemAdapter = new SearchItemAdapter(searchSubItemOnClick);
    displaySubsBinding.searchSubsRecyclerview.setAdapter(searchItemAdapter);
    displaySubsBinding.searchSubsRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

    // sets defaults for the actionbar
    myToolbar = displaySubsBinding.getRoot().findViewById(R.id.display_subs_toolbar);
    ((TextView) myToolbar.findViewById(R.id.my_toolbar_title)).setText(getText(R.string.app_name));

    AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
    if (parentActivity != null) {
      parentActivity.setSupportActionBar(myToolbar);
    }

    // attach the actions associated with loading the posts
    attachScrollListeners();
    initializeLivedata();

    displaySubsBinding.pinnedSubs.delegate = viewModel;

    return displaySubsBinding.getRoot();
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // calls method to subscribe the adapter to the livedata list
    observeLivedata();
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.search_menu, menu);

    searchMenuItem = menu.findItem(R.id.search_item);
    searchView = (SearchView) searchMenuItem.getActionView();

    // inialize a few of the view properties for the searchvie
    int padding = (int) getResources().getDimension(R.dimen.search_padding);
    searchView.setPadding(0, 0, padding, padding);

    // sets the listener for the search item on the menu to capture expand and collapse events
    searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(MenuItem menuItem) {
        // update visibility for the search recyclerview
        displaySubsBinding.setSearchIsVisible(true);
        displaySubsBinding.setSubscribedListIsVisible(false);
        return true;
      }

      @Override
      public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        // update visibility for the search recyclerview
        displaySubsBinding.setSearchIsVisible(false);
        displaySubsBinding.setSubscribedListIsVisible(true);

        // clear items in the adapter
        searchItemAdapter.clearSearchResults();
        return true;
      }
    });

    // Add query listeners to the searchview
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        viewModel.retrieveSearchResults(query);
        return false;
      }

      @Override
      public boolean onQueryTextChange(String query) {
        // sends search query to viewmodel, use submit because we search whenever query is changed
        return onQueryTextSubmit(query);
      }
    });
  }


  /**
   * Subscribes to various livedata values from the viewmodel
   */
  private void observeLivedata() {
    // allows the list to be updated as data is updated
    viewModel.getSubscribedList().observe(this, (List<SubredditModel> subredditsList) -> {
      // updates the view once the list is loaded
      if (subredditsList != null && !subredditsList.isEmpty()) {
        subAdapter.setList(new ArrayList<>(subredditsList));
        Log.d(TAG, "Changes to subreddit list received " + subredditsList);
      }
    });

    // observe whether all subscribed subreddits have been loaded
    viewModel.getAllSubscribedSubsLoaded().observe(this, (Boolean completelyLoaded) -> {
      Log.d(TAG, "Refreshing status changed to " + completelyLoaded);
      if (completelyLoaded) {
        swipeRefreshLayout.setRefreshing(false);
        displaySubsBinding.displaySubsRecyclerview.scrollToPosition(0);
        displaySubsBinding.setSubscribedListIsVisible(true);
      }
    });

    // subscribes to search results
    viewModel.getSearchResults().observe(this, new Observer<List<String>>() {
      @Override
      public void onChanged(@Nullable List<String> results) {
        // update the view based on search results
        if (results != null) {
          //Toast.makeText(getContext(), " " + results.toString(), Toast.LENGTH_SHORT).show();
          Log.d(TAG, " " + results.toString());
          searchItemAdapter.setSearchResults(results);
        }
      }
    });
  }


  private void initializeLivedata() {
//    // initialize livedata properties for binding
//    searchIsVisible = new MutableLiveData<>();
//    // initialize observer for updating binding on change
//    searchIsVisible.observe(this, (Boolean isVisible) -> {
//      displaySubsBinding.setSearchIsVisible(isVisible);
//    });
//    searchIsVisible.setValue(false);
  }


  public void attachScrollListeners() {
    swipeRefreshLayout = displaySubsBinding.getRoot().findViewById(R.id.display_subs_swiperefreshlayout);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        displaySubsBinding.setSubscribedListIsVisible(false);

        // refresh the current list and retrieve more posts
        subAdapter.resetSubList();
        viewModel.retrieveMoreSubs(true);
      }
    });
  }


  @Override
  public void onAllSubsLoaded() {
    //swipeRefreshLayout.setRefreshing(false);
  }


  /**
   * onClick class for the xml file to hook to
   */
  class OnClickSubItem implements SubItemOnClick {
    public void onClick(SubredditModel subItem) {
      Log.d(TAG, subItem.getSubName());

      // add the subreddit object to the bundle
      Bundle bundle = new Bundle();
      bundle.putString("SubredditName", subItem.getSubName());

      DisplaySubView subFrag = new DisplaySubView();
      subFrag.setArguments(bundle);

      // replace the current screen with the newly created fragment
      getActivity().getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit();
    }
  }


  class OnClickSearchSubItem implements SearchSubItemOnClick {
    LifecycleOwner owner;

    OnClickSearchSubItem (Fragment fragment) {
      // initializes reference to a lifecycle owner
      owner = fragment;
    }

    @Override
    public void onClick(String subName) {
      Log.d(TAG, "Search item clicked");

      // add the subreddit object to the bundle
      Bundle bundle = new Bundle();
      bundle.putString("SubredditName", subName);

      DisplaySubView subFrag = new DisplaySubView();
      subFrag.setArguments(bundle);

      // clear items in the adapter
      searchItemAdapter.clearSearchResults();

      // replace the current screen with the newly created fragment
      getActivity().getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit();

    }
  }


}
