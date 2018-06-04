package com.relic.presentation.displaysub;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.relic.R;
import com.relic.databinding.DisplaySubBinding;

public class DisplaySubView extends Fragment {
  DisplaySubContract.ViewModel displaySubVM;

  private DisplaySubBinding displaySubBinding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // initialize the databinding for the layout
    displaySubBinding = DataBindingUtil.inflate(inflater, R.layout.display_sub, container, false);

    return displaySubBinding.getRoot();
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // provide the viewmodel after the view has been initialized
    displaySubVM = ViewModelProviders.of(this).get(DisplaySubVM.class);

    Toast.makeText(this.getContext(), "Orientation changed", Toast.LENGTH_SHORT).show();
  }

}
