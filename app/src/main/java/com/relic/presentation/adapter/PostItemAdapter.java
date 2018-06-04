package com.relic.presentation.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.relic.domain.Post;

import java.util.List;

public class PostItemAdapter extends RecyclerView.Adapter<PostItemAdapter.PostItemVH>{
  List<Post> postList;

  @NonNull
  @Override
  public PostItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return null;
  }

  @Override
  public void onBindViewHolder(@NonNull PostItemVH holder, int position) {

  }

  @Override
  public int getItemCount() {
    return 0;
  }

  /**
   * Viewholder item for storing post view bindings
   */
  class PostItemVH extends RecyclerView.ViewHolder {

    public PostItemVH(View itemView) {
      super(itemView);
    }
  }



}
