package com.relic.presentation.displaysubs.subslist;

import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.relic.R;
import com.relic.domain.models.SubredditModel;
import com.relic.databinding.SubItemBinding;
import com.relic.presentation.adapter.SubItemOnClick;

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

  // TODO extract to external class once we decide design for sub items
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
      calculateDiff(newSubs).dispatchUpdatesTo(this);
      subList = newSubs;
  }

  private DiffUtil.DiffResult calculateDiff(List<SubredditModel>  newSubs) {
    return DiffUtil.calculateDiff(new DiffUtil.Callback() {
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
        return (subList.get(oldItemPosition).getId().equals(newSubs.get(newItemPosition).getId()));
      }

      @Override
      public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return true;
      }
    });
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
