package com.relic.presentation.displaysub;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.presentation.displaysubs.DisplaySubsContract;

public class DisplaySubView extends Fragment {
  DisplaySubContract.ViewModel displaySubVM;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // provide the viewmodel after the view has been initialized
    displaySubVM = ViewModelProviders.of(this).get(DisplaySubVM.class);
  }


}
