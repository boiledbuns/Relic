package com.relic.presentation.displaysubs;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.data.SubRepository;
import com.relic.data.SubRepositoryImpl;
import com.relic.databinding.DisplaySubsBinding;
import com.relic.domain.Subreddit;
import com.relic.presentation.adapter.SubItemAdapter;

import java.util.List;

public class DisplaySubsView extends Fragment {
  DisplaySubsContract.VM viewModel;
  public View rootView;

  private DisplaySubsBinding displaySubsBinding;
  private RecyclerView recyclerView;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // retrieve the instance of the viewmodel and attach a reference to it in this view
    viewModel = ViewModelProviders.of(this).get(DisplaySubsVM.class);

    // initialize the repository and inject it into the viewmodel
    SubRepository accountRepo = new SubRepositoryImpl(this.getContext());
    viewModel.init(accountRepo);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // inflate the databinding view
    displaySubsBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_subs, container, false);

    // initialize the adapter for the subs and attach it to the recyclerview
    SubItemAdapter subAdapter = new SubItemAdapter();
    displaySubsBinding.displaySubsRecyclerview.setAdapter(subAdapter);

    rootView = displaySubsBinding.getRoot();
    return displaySubsBinding.getRoot();
  }
}
