package com.relic.presentation.displaysubs;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.data.Authenticator;
import com.relic.data.SubRepository;
import com.relic.data.SubRepositoryImpl;
import com.relic.data.models.SubredditModel;
import com.relic.databinding.DisplaySubsBinding;
import com.relic.presentation.adapter.SubItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class DisplaySubsView extends Fragment {
  private final String TAG = "DISPLAY_SUBS_VIEW";
  DisplaySubsContract.VM viewModel;
  public View rootView;

  private DisplaySubsBinding displaySubsBinding;
  SubItemAdapter subAdapter;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // retrieve the instance of the viewmodel and attach a reference to it in this view
    viewModel = ViewModelProviders.of(this).get(DisplaySubsVM.class);

    // initialize the repository and inject it into the viewmodel
    SubRepository accountRepo = new SubRepositoryImpl(this.getContext());
    viewModel.init(accountRepo, Authenticator.getAuthenticator(this.getContext()));
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // inflate the databinding view
    displaySubsBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_subs, container, false);

    // initialize the adapter for the subs and attach it to the recyclerview
    subAdapter = new SubItemAdapter(this.getContext());
    displaySubsBinding.displaySubsRecyclerview.setAdapter(subAdapter);

    // displays the items in 3 columns
    ((GridLayoutManager) displaySubsBinding.displaySubsRecyclerview.getLayoutManager())
        .setSpanCount(3);

    // calls method to subscribe the adapter to the livedata list
    subscribeToList(viewModel);

    rootView = displaySubsBinding.getRoot();
    return displaySubsBinding.getRoot();
  }

  private void subscribeToList(DisplaySubsContract.VM viewModel) {
    // allows the list to be updated as data is updated
    viewModel.getSubscribedList().observe(this, new Observer<List<SubredditModel>>() {
      @Override
      public void onChanged(@Nullable List<SubredditModel> subredditsList) {
        // updates the view once the list is loaded
        if (subredditsList != null) {
          subAdapter.setList(new ArrayList<>(subredditsList));
          Log.d(TAG, "Changes to subreddit list received");
        }
        // execute changes and sync
        displaySubsBinding.executePendingBindings();
      }
    });
  }
}
