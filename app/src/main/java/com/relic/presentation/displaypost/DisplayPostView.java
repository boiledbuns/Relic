package com.relic.presentation.displaypost;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.relic.R;
import com.relic.data.models.PostModel;
import com.relic.databinding.DisplayPostBinding;
import com.relic.presentation.callbacks.PostLoadCallback;

public class DisplayPostView extends Fragment {
  private final String TAG = "DISPLAYPOST_VIEW";
  private DisplayPostContract.ViewModel displayPostView;
  private DisplayPostBinding postBinding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    postBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_post, container, false);

    // parse the full name of the post to be displayed
    try {
      String postFullname = getArguments().getString("full_name");
      Log.d(TAG, "Post fullname : " + postFullname);
    }
    catch (NullPointerException e) {
      Toast.makeText(getContext(), "Fragment not loaded properly!", Toast.LENGTH_SHORT);
    }

    return postBinding.getRoot();
  }




}
