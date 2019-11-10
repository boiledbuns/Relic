package com.relic.presentation.displaysubs.subslist;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.relic.domain.models.SubredditModel;
import com.relic.presentation.displaysubs.DisplaySubsContract;

import java.util.ArrayList;
import java.util.List;

public class SubItemAdapter extends RecyclerView.Adapter<SubItemAdapter.SubItemVH> {
  private List<SubredditModel> subList = new ArrayList<>();
  private DisplaySubsContract.SubAdapterDelegate subAdapterDelegate;

  public SubItemAdapter(DisplaySubsContract.SubAdapterDelegate subAdapterDelegate) {
    this.subAdapterDelegate = subAdapterDelegate;
  }

  class SubItemVH extends RecyclerView.ViewHolder {
    SubItemView subItemView;

    SubItemVH(SubItemView subItemView) {
      super(subItemView);
      this.subItemView = subItemView;

      subItemView.setOnClickListener(v ->
          subAdapterDelegate.interact(
              subList.get(getAdapterPosition()),
              DisplaySubsContract.SubInteraction.Visit.INSTANCE
          )
      );

      subItemView.setOnLongClickListener(v -> {
        subAdapterDelegate.interact(
            subList.get(getAdapterPosition()),
            DisplaySubsContract.SubInteraction.Preview.INSTANCE
        );
        return true;
      });
    }

    void bind(SubredditModel subModel) { subItemView.bind(subModel); }
  }


  @NonNull
  @Override
  public SubItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    SubItemView subItemView = new SubItemView(parent.getContext());
    return new SubItemVH(subItemView);
  }

  @Override
  public void onBindViewHolder(@NonNull SubItemVH holder, int position) {
    holder.bind(subList.get(position));
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
