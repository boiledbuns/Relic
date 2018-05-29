package com.relic.presentation.DisplaySubs;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.data.SubRepository;
import com.relic.data.SubRepositoryImpl;

public class DisplaySubsView extends Fragment {
  DisplaySubsContract.VM viewModel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = ViewModelProviders.of(this).get(DisplaySubsVM.class);

    // initialize the repository and inject it into the viewmodel
    SubRepository accountRepo = new SubRepositoryImpl(this.getContext());
    viewModel.init(accountRepo);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return super.onCreateView(inflater, container, savedInstanceState);
  }
}
