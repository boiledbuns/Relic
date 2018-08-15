package com.relic.presentation.displaysubinfo;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.relic.R;
import com.relic.data.SubRepositoryImpl;
import com.relic.data.models.SubredditModel;
import com.relic.databinding.DisplaySubInfoBinding;
import com.relic.presentation.displaysub.DisplaySubContract;
import com.relic.presentation.displaysub.DisplaySubVM;
import com.squareup.picasso.Picasso;

public class DisplaySubInfoView extends BottomSheetDialogFragment{
  private String TAG = "DISPLAYSUBINFO_VIEW";

  private DisplaySubInfoContract.ViewModel displaySubInfoVM;
  private DisplaySubContract.ViewModel displaySubVM;

  private View displaySubInfoView;
  // private DisplaySubInfoBinding displaySubInfoBinding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    // retrieve the name of the subreddit form the args
    String subName = getArguments().getString("name");

    super.onCreate(savedInstanceState);
//    displaySubInfoVM = ViewModelProviders.of(this).get(DisplaySubInfoVM.class);
//    displaySubInfoVM.initialize(subName, new SubRepositoryImpl(getContext()));

    displaySubVM = ViewModelProviders.of(getActivity()).get(DisplaySubVM.class);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    displaySubInfoView = inflater.inflate(R.layout.display_sub_info, container, false);
    // displaySubInfoBinding = DataBindingUtil.bind(LayoutInflater.from(getActivity()).inflate(R.layout.display_sub_info, container, false));
    subscribeToVM();
    loadSubIcon();

    return displaySubInfoView;
  }

  private void loadSubIcon() {
    // observe the livedata for sub model and load the image once it loads
    displaySubVM.getSubModel().observe(this, (SubredditModel subModel) -> {

      ImageView icon = displaySubInfoView.findViewById(R.id.display_subinfo_icon);
      String iconUrl = subModel.getSubIcon();

      if (iconUrl != null && !iconUrl.isEmpty()) {
        Log.d(TAG, "Loading icon for " + subModel.getSubName() + " : " + subModel.getSubIcon());
        Picasso.get().load(subModel.getSubIcon()).fit().centerCrop().into(icon);
      }

    });
  }

//  @NonNull
//  @Override
//  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//    displaySubinfoBinding = DataBindingUtil.bind(LayoutInflater.from(getActivity()).inflate(R.layout.display_sub_info, null, false));
//
//    // override dialog creation to add custom buttons
//    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//    builder.setView(displaySubinfoBinding.getRoot())
//        .setPositiveButton("UNSUBSCRIBE", new DialogInterface.OnClickListener() {
//          @Override
//          public void onClick(DialogInterface dialogInterface, int i) {
//            // display the button to subscribe if the user is not subscribed (and vice versa)
//            displaySubInfoVM.subscribe();
//          }
//        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
//          @Override
//          public void onClick(DialogInterface dialogInterface, int i) {
//          }
//        });
//    subscribeToVM();
//
//    return builder.create();
//  }


  /**
   * Add observers and onchange actions to all appropriate live data
   */
  private void subscribeToVM() {
//    displaySubInfoVM.getSubreddit().observe(this, (SubredditModel subModel) -> {
//      if (subModel == null) {
//        Log.d(TAG, "EMPTY");
//        // retrieve single subreddit from api
//        displaySubInfoVM.retrieveSubreddit();
//      }
//      else {
//        Log.d(TAG, "NOT EMPTY");
//        displaySubInfoBinding.setSubModel(subModel);
//      }
//      displaySubInfoBinding.executePendingBindings();
//    });

    // observe the subreddit description
//    displaySubInfoVM.getDescription().observe(this, new Observer<String>() {
//      @Override
//      public void onChanged(@Nullable String description) {
//        if (description != null) {
//          Log.d(TAG, "Change observed " + description);
//          //displaySubinfoBinding.setSubDescription(description);
//          displaySubinfoBinding.executePendingBindings();
//        }
//      }
//    });

//    displaySubInfoVM.getSubscribed().observe(this,
//        (Boolean isSubscribed) -> //displaySubinfoBinding.setSubscribed(isSubscribed)
//    );
  }
}
