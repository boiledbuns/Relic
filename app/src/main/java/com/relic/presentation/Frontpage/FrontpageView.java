package com.relic.presentation.Frontpage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.data.PostRepository;
import com.relic.data.PostRepositoryImpl;

public class FrontpageView extends Fragment implements FrontpageContract.View{
  View rootView;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.frontpage, container, false);



    return rootView;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // initializes the instance of the post repo and injects it into the VM
    PostRepository postRepo = new PostRepositoryImpl(getContext());

  }


}
