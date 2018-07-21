package com.relic.presentation.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.databinding.SearchSubItemBinding;

import java.util.List;

public class SearchItemAdapter extends RecyclerView.Adapter <SearchItemAdapter.SearchSubItemVH> {
  List <String> searchResults;

  class SearchSubItemVH extends RecyclerView.ViewHolder {
    SearchSubItemBinding searchSubItemBinding;

    public SearchSubItemVH(@NonNull View itemView) {
      super(itemView);
    }
  }

  @NonNull
  @Override
  public SearchSubItemVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    SearchSubItemBinding searchSubItemBinding = DataBindingUtil
        .inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.search_sub_item, viewGroup, false);

    return new SearchSubItemVH(searchSubItemBinding.getRoot());
  }

  @Override
  public void onBindViewHolder(@NonNull SearchSubItemVH searchItemVH, int position) {
    SearchSubItemBinding itemBinding = searchItemVH.searchSubItemBinding;
    itemBinding.setSubredditName(searchResults.get(position));
  }

  @Override
  public int getItemCount() {
    return searchResults.size();
  }

  public void setSearchResults(List<String> searchResults) {
    this.searchResults = searchResults;
  }
}
