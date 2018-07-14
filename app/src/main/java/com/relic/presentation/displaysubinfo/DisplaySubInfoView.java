package com.relic.presentation.displaysubinfo;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
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

public class DisplaySubInfoView extends DialogFragment{
  private DisplaySubInfoContract.ViewModel viewModel;

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
    // inflate custom layout for our dialog
    View view = inflater.inflate(R.layout.display_subinfo, container, false);
    return view;
  }


  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    // display additionla data for the subreddit (ie. sidebar)

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
}
