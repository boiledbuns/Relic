package com.relic.presentation.displaysubinfo;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.data.SubRepositoryImpl;
import com.relic.data.gateway.SubGateway;
import com.relic.data.gateway.SubGatewayImpl;
import com.relic.databinding.DisplaySubinfoBinding;

public class DisplaySubInfoView extends DialogFragment{
  private DisplaySubInfoContract.ViewModel viewModel;
  private DisplaySubinfoBinding displaySubinfoBinding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    // retrieve the name of the subreddit form the args
    String subName = getArguments().getString("name");

    super.onCreate(savedInstanceState);
    viewModel = ViewModelProviders.of(this).get(DisplaySubInfoVM.class);
    viewModel.initialize(subName, new SubRepositoryImpl(getContext()));
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // initialize the databinding with the inflater
    displaySubinfoBinding = DataBindingUtil.bind(inflater.inflate(R.layout.display_subinfo, container, false));
    subscribeToVM();
    return displaySubinfoBinding.getRoot();
  }


  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    // display additional data for the subreddit (ie. sidebar)

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage("TEST")
        .setPositiveButton("SUBSCRIBE", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // display the button to subscribe if the user is not subscribed (and vice versa)

            //subGateway.subscribe();
          }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // TODO set no
          }
        });
    return builder.create();
  }


  private void subscribeToVM() {
    // observe the subreddit description
    viewModel.getDescription().observe(this, new Observer<String>() {
      @Override
      public void onChanged(@Nullable String description) {
        if (description != null) {
          displaySubinfoBinding.setSubDescription(description);
        }
      }
    });

    viewModel.getSubscribed().observe(this,
        (Boolean isSubscribed) -> displaySubinfoBinding.setSubscribed(isSubscribed)
    );
  }
}
