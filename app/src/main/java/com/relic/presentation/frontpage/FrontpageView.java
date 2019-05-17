package com.relic.presentation.frontpage;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.data.auth.AuthImpl;
import com.relic.data.PostRepository;
import com.relic.data.PostRepositoryImpl;
import com.relic.network.NetworkRequestManager;

public class FrontpageView extends Fragment {
  FrontpageContract.VM viewModel;
  View rootView;

  /**
   * Recall: fragment calls "onCreate" once when it's first initialized, so this is where
   * we'll initialize and inject dependencies into the viewmodel
   * @param savedInstanceState
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // initializes the instance of the post repo and injects it into the ViewModel
    PostRepository postRepo = new PostRepositoryImpl(getContext(), new NetworkRequestManager(getContext()));
    AuthImpl auth = new AuthImpl(getContext());

    viewModel = ViewModelProviders.of(this).get(FrontpageVM.class);
    viewModel.init(postRepo, auth);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.frontpage, container, false);
    return rootView;
  }

}
