package com.relic.presentation.displaypost;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.databinding.DisplayPostBinding;

public class DisplayPostView extends Fragment {
  private DisplayPostContract.ViewModel displayPostView;
  private DisplayPostBinding postBinding;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    postBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_post, container, false);

    return postBinding.getRoot();
  }
}
