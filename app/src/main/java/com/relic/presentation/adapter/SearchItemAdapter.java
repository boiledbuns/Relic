package com.relic.presentation.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.databinding.SearchSubItemBinding;

import java.util.List;

public class SearchItemAdapter extends RecyclerView.Adapter <SearchItemAdapter.SearchSubItemVH> {
  private final String TAG = "SEARCH_SUBITEM_ADAPTER";
  private List <String> searchResults;

  class SearchSubItemVH extends RecyclerView.ViewHolder {
    SearchSubItemBinding searchSubItemBinding;

    SearchSubItemVH(@NonNull SearchSubItemBinding subItemBinding) {
      super(subItemBinding.getRoot());
      this.searchSubItemBinding = subItemBinding;
    }
  }

  @NonNull
  @Override
  public SearchSubItemVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    SearchSubItemBinding searchSubItemBinding = DataBindingUtil
        .inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.search_sub_item, viewGroup, false);

    return new SearchSubItemVH(searchSubItemBinding);
  }

  @Override
  public void onBindViewHolder(@NonNull SearchSubItemVH searchItemVH, int position) {
    Log.d(TAG, "Binding " + searchResults.get(position));

    SearchSubItemBinding itemBinding = searchItemVH.searchSubItemBinding;
    itemBinding.setSubredditName(searchResults.get(position));
    itemBinding.executePendingBindings();
  }

  @Override
  public int getItemCount() {
    return searchResults != null ? searchResults.size() : 0;
  }

  public void setSearchResults(List<String> searchResults) {
    if (searchResults != null) {
      Log.d(TAG, "Search results received " + searchResults);
      this.searchResults = searchResults;
      notifyDataSetChanged();
    }
  }
}
