package com.relic.presentation.adapter;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.relic.R;
import com.relic.data.models.SubredditModel;
import com.relic.databinding.SubItemBinding;
import com.relic.presentation.displaysub.DisplaySubView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SubItemAdapter extends RecyclerView.Adapter<SubItemAdapter.SubItemVH> {
  private final String TAG = "SUB_ITEM_ADAPTER";
  private List<SubredditModel> subList = new ArrayList<>();
  private Context fragmentContext;

  public SubItemAdapter(Context context) {
    super();
    // initialize the context for this app to be used by the inner class
    fragmentContext = context;
  }

  /**
   * Viewholder to cache data
   */
  class SubItemVH extends RecyclerView.ViewHolder {
    final SubItemBinding binding;

    SubItemVH(SubItemBinding subBinding) {
      super(subBinding.getRoot());
      this.binding = subBinding;

      // attach the onclick to this item
      this.binding.setItemOnClick(new SubItemOnClick());
    }
  }


  @NonNull
  @Override
  public SubItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // initialize the new viewholder with the binding to the sub item
    SubItemBinding subBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
        R.layout.sub_item, parent, false);

    return new SubItemVH(subBinding);
  }

  @Override
  public void onBindViewHolder(@NonNull SubItemVH holder, int position) {
    // attachs the sub item to the viewholder post binding
    holder.binding.setSubredditItem(subList.get(position));
    holder.binding.executePendingBindings();
  }

  @Override
  public int getItemCount() {
    return subList.size();
  }


  /**
   *
   * @param subs list of all posts
   */
  public void setList(List<SubredditModel> subs) {
    // set the entire list if the current list is null
    if (subs == null) {
      this.subList = new ArrayList<>();
    }
    else if (this.subList.size() == 0) {
      this.subList = subs;
      notifyItemChanged(0, subs.size());
      Log.d(TAG, subs.size() + " ");
    }
  }

  @BindingAdapter({"bind:bannerUrl"})
  public static void loadImage(ImageView imgView, String bannerUrl) {
    // does not load image if the banner img string is empty
    if (bannerUrl.length() > 0) {
      try {
        Log.d("SUB_ITEM_ADAPTER", "URL = " + bannerUrl);
        Picasso.get().load(bannerUrl).into(imgView);
      }
      catch (Error e) {
        Log.d("SUB_ITEM_ADAPTER", "Issue loading image " + e.toString());
      }
    }
  }


  /**
   * onclick class for the xml file to hook to
   */
  public class SubItemOnClick {
    public void onClick(SubredditModel subItem) {
      Log.d(TAG, subItem.getSubName());

      // add the sub reddit object to the bundle
      Bundle bundle = new Bundle();
      bundle.putParcelable("SubredditModel", subItem);

      DisplaySubView subFrag = new DisplaySubView();
      subFrag.setArguments(bundle);

      // replace the current screen with the newly created fragment
      ((FragmentActivity) fragmentContext).getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit();
    }
  }

}
