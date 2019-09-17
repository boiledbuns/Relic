package com.relic.presentation.displaysub;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.relic.R;
import com.relic.domain.models.SubredditModel;
import com.relic.databinding.DisplaySubInfoBinding;
import com.relic.presentation.base.RelicBottomSheetDialog;
import com.relic.presentation.displaysubinfo.DisplaySubInfoContract;
import com.squareup.picasso.Picasso;

@Deprecated
public class DisplaySubInfoView extends RelicBottomSheetDialog {
  private String TAG = "DISPLAYSUBINFO_VIEW";

  private DisplaySubInfoContract.ViewModel displaySubInfoVM;
  private DisplaySubVM displaySubVM;

  private View displaySubInfoView;
  private DisplaySubInfoBinding displaySubInfoBinding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    // retrieve the name of the subreddit form the args
    String subName = getArguments().getString("name");

    super.onCreate(savedInstanceState);
//    displaySubInfoVM = ViewModelProviders.of(this).get(DisplaySubInfoVM.class);
//    displaySubInfoVM.initialize(subName, new SubRepositoryImpl(getContext()));
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    displaySubInfoBinding = DisplaySubInfoBinding.bind(inflater.inflate(R.layout.display_sub_info, container, false));
    displaySubInfoBinding.setDisplaySubVM(displaySubVM);

    subscribeToVM();
    loadSubIcon();

    return displaySubInfoBinding.getRoot();
  }

  private void loadSubIcon() {
    // observe the livedata for sub model and load the image once it loads
    displaySubVM.getSubredditLiveData().observe(this, (SubredditModel subModel) -> {

      ImageView icon = displaySubInfoBinding.getRoot().findViewById(R.id.display_subinfo_icon);
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
    displaySubVM.getSubredditLiveData().observe(this, (@NonNull SubredditModel subModel) -> {
      // updates the submodel bound to the view
      displaySubInfoBinding.setSubModel(subModel);
    });

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
