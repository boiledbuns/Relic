package com.relic.presentation.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentItemVH>{
  class CommentItemVH extends RecyclerView.ViewHolder {
    public CommentItemVH(View itemView) {
      super(itemView);
    }
  }

  @NonNull
  @Override
  public CommentItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return null;
  }

  @Override
  public void onBindViewHolder(@NonNull CommentItemVH holder, int position) {

  }

  @Override
  public int getItemCount() {
    return 0;
  }


}
