package com.relic.presentation.adapter;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.relic.R;
import com.relic.domain.models.SubredditModel;
import com.relic.databinding.SubItemBinding;

import java.util.ArrayList;
import java.util.List;

public class SubItemAdapter extends RecyclerView.Adapter<SubItemAdapter.SubItemVH> {
  private final String TAG = "SUB_ITEM_ADAPTER";
  private List<SubredditModel> subList = new ArrayList<>();
  private SubItemOnClick onClick;

  public SubItemAdapter(SubItemOnClick onClick) {
    super();
    this.onClick = onClick;
  }

  /**
   * Viewholder to cache data
   */
  class SubItemVH extends RecyclerView.ViewHolder {
    final SubItemBinding binding;

    SubItemVH(SubItemBinding subBinding) {
      super(subBinding.getRoot());
      this.binding = subBinding;
    }
  }


  @NonNull
  @Override
  public SubItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // initialize the new viewholder with the binding to the sub item
    SubItemBinding subBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
        R.layout.sub_item, parent, false);

    subBinding.setSubOnClick(onClick);
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
   * @param newSubs list of all posts
   */
  public void setList(List<SubredditModel> newSubs) {
    // set the entire list if the current list is null
    if (subList.size() == 0) {
      this.subList = newSubs;
      notifyDataSetChanged();
    }
    else {
      DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
        @Override
        public int getOldListSize() {
          return subList.size();
        }

        @Override
        public int getNewListSize() {
          return newSubs.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
          return (subList.get(oldItemPosition).id.equals(newSubs.get(newItemPosition).id));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
          return true;
        }
      });

      subList = newSubs;
      diffResult.dispatchUpdatesTo(this);
    }
  }

  @BindingAdapter({"bind:bannerUrl"})
  public static void loadImage(ImageView imgView, String bannerUrl) {
    // does not load image Wif the banner img string is empty
//    if (bannerUrl.length() > 0) {
//      try {
//        Log.d("SUB_ITEM_ADAPTER", "URL = " + bannerUrl);
//        Picasso.get().load(bannerUrl).into(imgView);
//      }
//      catch (Error e) {
//        Log.d("SUB_ITEM_ADAPTER", "Issue loading image " + e.toString());
//      }
//    }
  }

  public void clearList() {
    subList.clear();
    notifyDataSetChanged();
  }
}
