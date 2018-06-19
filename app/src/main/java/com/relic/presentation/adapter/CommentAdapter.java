package com.relic.presentation.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.data.models.CommentModel;
import com.relic.databinding.CommentItemBinding;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentItemVH>{
  private final String TAG = "COMMENT_ADAPTER";
  private List<CommentModel> commentList;

  class CommentItemVH extends RecyclerView.ViewHolder {
    CommentItemBinding commentItemBinding;

    public CommentItemVH(CommentItemBinding commentItemBinding) {
      super(commentItemBinding.getRoot());

      this.commentItemBinding = commentItemBinding;
    }
  }


  @NonNull
  @Override
  public CommentItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // create a new binding for the comment
    CommentItemBinding commentItemBinding = DataBindingUtil.inflate(
        LayoutInflater.from(parent.getContext()), R.layout.comment_item, parent, false);

    CommentItemVH commentItemVH = new CommentItemVH(commentItemBinding);
    return commentItemVH;
  }


  @Override
  public void onBindViewHolder(@NonNull CommentItemVH holder, int position) {
    // attach the current binding to the viewholder
    holder.commentItemBinding.setCommentModel(commentList.get(position));
    Log.d(TAG, "Comment " + commentList.get(position).getBody());
    holder.commentItemBinding.executePendingBindings();
  }


  @Override
  public int getItemCount() {
    return commentList == null ? 0 : commentList.size();
  }


  public void setComments(List<CommentModel> newComments) {
    if (commentList == null) {
      commentList = newComments;
      Log.d(TAG, "No comments " + getItemCount());
    } else {
      // use diff util to check the difference
      commentList.addAll(newComments);
      Log.d(TAG, "Comments " + commentList.size());
    }
    notifyItemChanged(0, commentList.size());
  }
}
