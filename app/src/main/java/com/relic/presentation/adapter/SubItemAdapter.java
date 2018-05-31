package com.relic.presentation.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.databinding.SubItemBinding;

public class SubItemAdapter extends RecyclerView.Adapter<SubItemAdapter.SubItemVH> {
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

    return new SubItemVH(subBinding);
  }

  @Override
  public void onBindViewHolder(@NonNull SubItemVH holder, int position) {

  }

  @Override
  public int getItemCount() {
    return 0;
  }
}
