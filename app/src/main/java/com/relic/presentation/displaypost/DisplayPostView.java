package com.relic.presentation.displaypost;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import com.relic.data.CommentRepositoryImpl;
import com.relic.data.PostRepositoryImpl;
import com.relic.data.models.PostModel;
import com.relic.databinding.DisplayPostBinding;
import com.relic.presentation.callbacks.PostLoadCallback;

public class DisplayPostView extends Fragment {
  private final String TAG = "DISPLAYPOST_VIEW";
  private DisplayPostContract.ViewModel displayPostVM;
  private DisplayPostBinding postBinding;
  private String postFullname;


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    postBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_post, container, false);

    try {
      // parse the full name of the post to be displayed
      postFullname = getArguments().getString("full_name");
      Log.d(TAG, "Post fullname : " + postFullname);
    }
    catch (Exception e) {
      Toast.makeText(getContext(), "Fragment not loaded properly!", Toast.LENGTH_SHORT).show();
    }

    return postBinding.getRoot();
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // create the VM and initialize it with injected dependencies
    displayPostVM = ViewModelProviders.of(this).get(DisplayPostVM.class);
    displayPostVM.init(new PostRepositoryImpl(getContext()),
        new CommentRepositoryImpl(getContext()), postFullname);

    subscribeToVM();
  }


  /**
   * subscribes the view to the data exposed by the viewmodel
   */
  private void subscribeToVM() {
    // Observe the post exposed by the VM
    displayPostVM.getPost().observe(this, new Observer<PostModel>() {
      @Override
      public void onChanged(@Nullable PostModel postModel) {
        if (postModel != null) {
          postBinding.setPostItem(postModel);
        }
      }
    });

    // TODO : Observe the comments exposed by the VM
  }


}
