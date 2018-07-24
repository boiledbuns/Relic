package com.relic.presentation.displaysubs;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.relic.MainActivity;
import com.relic.R;
import com.relic.data.ApplicationDB;
import com.relic.data.Authenticator;
import com.relic.data.ListingRepositoryImpl;
import com.relic.data.SubRepository;
import com.relic.data.SubRepositoryImpl;
import com.relic.data.models.SubredditModel;
import com.relic.databinding.DisplaySubsBinding;
import com.relic.presentation.adapter.SearchItemAdapter;
import com.relic.presentation.adapter.SearchSubItemAdapter;
import com.relic.presentation.adapter.SubItemAdapter;
import com.relic.presentation.adapter.SubItemOnClick;
import com.relic.presentation.callbacks.AllSubsLoadedCallback;
import com.relic.presentation.displaysub.DisplaySubView;

import java.util.ArrayList;
import java.util.List;

public class DisplaySubsView extends Fragment implements AllSubsLoadedCallback{
  private final String TAG = "DISPLAY_SUBS_VIEW";
  DisplaySubsContract.VM viewModel;
  public View rootView;

  private SearchView searchView;
  private MenuItem searchMenuItem;

  private DisplaySubsBinding displaySubsBinding;
  private SwipeRefreshLayout swipeRefreshLayout;
  SubItemAdapter subAdapter;
  SearchItemAdapter searchItemAdapter;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    // retrieve the instance of the viewmodel and attach a reference to it in this view
    viewModel = ViewModelProviders.of(this).get(DisplaySubsVM.class);

    // initialize the repository and inject it into the viewmodel
    viewModel.init(new SubRepositoryImpl(getContext()), new ListingRepositoryImpl(getContext()),
        Authenticator.getAuthenticator(this.getContext()));
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // inflate the databinding view
    displaySubsBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_subs, container, false);

    // initialize the adapters for the subs and attach it to the recyclerview
    subAdapter = new SubItemAdapter(new OnClick());
    displaySubsBinding.displaySubsRecyclerview.setAdapter(subAdapter);

    searchItemAdapter = new SearchItemAdapter();

    // displays the items in 3 columns
    ((GridLayoutManager) displaySubsBinding.displaySubsRecyclerview.getLayoutManager())
        .setSpanCount(3);

    rootView = displaySubsBinding.getRoot();

    // sets defaults for the actionbar
    getActivity().findViewById(R.id.my_toolbar_title).setOnClickListener(null);
    ((MainActivity) getActivity()).customSetTitle(R.string.app_name, null);

    // attach the actions associated with loading the posts
    attachScrollListeners();

    return displaySubsBinding.getRoot();
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // calls method to subscribe the adapter to the livedata list
    subscribeToLiveData();
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

    Fragment currentFragment = this;
    // Add query listeners to the searchview
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        LiveData <List<String>> searchResults = viewModel.retrieveSearchResults(query);

        // sends search query to viewmodel and attach a listener to the livedata returned
        if (searchResults != null) {
          searchResults.observe(currentFragment, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> results) {
              Toast.makeText(currentFragment.getContext(), " " + results.toString(), Toast.LENGTH_SHORT).show();
            }
          });
        }
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
   * Gets livedata list of subscribed subs from the VM and attach a listener to it
   */
  private void subscribeToLiveData() {
    // allows the list to be updated as data is updated
    viewModel.getSubscribedList().observe(this, new Observer<List<SubredditModel>>() {
      @Override
      public void onChanged(@Nullable List<SubredditModel> subredditsList) {
        // updates the view once the list is loaded
        if (subredditsList != null) {
          subAdapter.setList(new ArrayList<>(subredditsList));
          Log.d(TAG, "Changes to subreddit list received");
          swipeRefreshLayout.setRefreshing(false);
        }
        // execute bindings only once everything is loaded (on callback)
        // execute changes and sync
        displaySubsBinding.executePendingBindings();
      }
    });
  }


  public void attachScrollListeners() {
    swipeRefreshLayout = displaySubsBinding.getRoot().findViewById(R.id.display_subs_swiperefreshlayout);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        // refresh the current list and retrieve more posts
        subAdapter.resetSubList();
        viewModel.retrieveMoreSubs(true);
        // TODO start the loading animation for the screen
      }
    });
  }


  @Override
  public void onAllSubsLoaded() {
    // TODO: stop the loading animation
  }


  /**
   * onClick class for the xml file to hook to
   */
  public class OnClick implements SubItemOnClick {
    public void onClick(SubredditModel subItem) {
      Log.d(TAG, subItem.getSubName());

      // add the subreddit object to the bundle
      Bundle bundle = new Bundle();
      bundle.putParcelable("SubredditModel", subItem);

      DisplaySubView subFrag = new DisplaySubView();
      subFrag.setArguments(bundle);

      // replace the current screen with the newly created fragment
      getActivity().getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit();
    }
  }

  static class AttachSearchCursor extends AsyncTask<String, Cursor, Cursor> {
    Context context;
    SearchView searchView;

    AttachSearchCursor(SearchView searchView, Context context) {
      this.context = context;
      this.searchView = searchView;
    }

    @Override
    protected Cursor doInBackground(String... strings) {
      //TODO testing
      SubRepository subRepo = new SubRepositoryImpl(context);
      return ApplicationDB.getDatabase(context).getSubredditDao().searchSubreddits("test");
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
      SearchSubItemAdapter adapter = new SearchSubItemAdapter(context, cursor, false);
      searchView.setSuggestionsAdapter(adapter);
    }
  }

}
