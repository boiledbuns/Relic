package com.relic.presentation.displaysub;

import android.app.Dialog;
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

public class DisplaySubInfoDialog extends DialogFragment{

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
    //
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage("TEST")
        .setPositiveButton("SUBSCRIBE", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // TODO set yes

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
