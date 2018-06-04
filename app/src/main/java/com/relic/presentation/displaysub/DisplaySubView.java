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
import com.relic.data.PostRepositoryImpl;
import com.relic.databinding.DisplaySubBinding;

public class DisplaySubView extends Fragment {
  DisplaySubContract.ViewModel displaySubVM;

  private DisplaySubBinding displaySubBinding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // get the viewmodel and inject the dependencies into it
    displaySubVM = ViewModelProviders.of(this).get(DisplaySubVM.class);

    // initialize a new post repo and inject it into the viewmodel
    // initialization occurs for vm only when the view is first created
    displaySubVM.init(new PostRepositoryImpl(this.getContext()));
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

    if (displaySubVM == null) {
      // fetch the viewmodel if the fragment survives a reconfiguration change
      displaySubVM = ViewModelProviders.of(this).get(DisplaySubVM.class);
    }

    Toast.makeText(this.getContext(), "Orientation changed", Toast.LENGTH_SHORT).show();
  }

}
