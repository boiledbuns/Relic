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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.data.SubRepositoryImpl;
import com.relic.data.gateway.SubGateway;
import com.relic.data.gateway.SubGatewayImpl;
import com.relic.databinding.DisplaySubinfoBinding;

public class DisplaySubInfoView extends DialogFragment{
  private String TAG = "DISPLAYSUBINFO_VIEW";

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


  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    displaySubinfoBinding = DataBindingUtil.bind(LayoutInflater.from(getActivity()).inflate(R.layout.display_subinfo, null, false));

    // override dialog creation to add custom buttons
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setView(displaySubinfoBinding.getRoot())
        .setPositiveButton("UNSUBSCRIBE", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // display the button to subscribe if the user is not subscribed (and vice versa)
            viewModel.subscribe();
          }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
          }
        });

    subscribeToVM();

    return builder.create();
  }


  private void subscribeToVM() {
    // observe the subreddit description
    viewModel.getDescription().observe(this, new Observer<String>() {
      @Override
      public void onChanged(@Nullable String description) {
        if (description != null) {
          Log.d(TAG, "Change observed " + description);
          displaySubinfoBinding.setSubDescription(description);
          displaySubinfoBinding.executePendingBindings();
        }
      }
    });

    viewModel.getSubscribed().observe(this,
        (Boolean isSubscribed) -> displaySubinfoBinding.setSubscribed(isSubscribed)
    );
  }
}
