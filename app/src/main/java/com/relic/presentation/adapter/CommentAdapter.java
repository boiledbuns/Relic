package com.relic.presentation.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;
import com.relic.data.models.CommentModel;
import com.relic.databinding.CommentItemBinding;
import com.relic.presentation.displaypost.DisplayPostContract;
import com.relic.presentation.displaypost.DisplayPostView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentItemVH>{
  private final String TAG = "COMMENT_ADAPTER";
  private List<CommentModel> commentList;

  private DisplayPostContract.PostViewDelegate delegate;

  public CommentAdapter(DisplayPostContract.PostViewDelegate postViewDelegate) {
    super();
    delegate = postViewDelegate;
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
    // attach the current binding to the viewholder and update its position value
    holder.bind( position);
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
      commentList = newComments;
      Log.d(TAG, "Comments " + commentList.size());

      DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
        @Override
        public int getOldListSize() {
          return commentList.size();
        }

        @Override
        public int getNewListSize() {
          return newComments.size();
        }

        @Override
        public boolean areItemsTheSame(int i, int i1) {
          return (commentList.get(i).getId().equals(newComments.get(i1).getId()));
        }

        @Override
        public boolean areContentsTheSame(int i, int i1) {
          CommentModel oldComment = commentList.get(i);
          CommentModel newComment = newComments.get(i1);

          boolean isSame = oldComment.getUserUpvoted() == newComment.getUserUpvoted();

          return isSame;
        }
      });

      commentList = newComments;
      diffResult.dispatchUpdatesTo(this);
    }
    notifyItemChanged(0, commentList.size());
  }

  /**
   *  Viewholder for comments
   */
  class CommentItemVH extends RecyclerView.ViewHolder {
    private CommentItemBinding commentItemBinding;
    private int itemPosition;

    public CommentItemVH(CommentItemBinding commentItemBinding) {
      super(commentItemBinding.getRoot());

      this.commentItemBinding = commentItemBinding;
      initializeItemOnClick();
    }

    void bind(int position) {
      commentItemBinding.setCommentModel(commentList.get(position));
      itemPosition = position;
    }

    /**
     * TODO remove logic from these methods, bubble up to viewmodel
     * initializes databinding with onclicks to prevent polluting binding with unnecessary handler
     * objects for handling onclicks
     */
    private void initializeItemOnClick () {
      // initialize onclicks for the views comment items
      commentItemBinding.commentitemUpvote.setOnClickListener((View view) -> {
        CommentModel commentModel = commentList.get(itemPosition);
        // determine the new vote value based on the current one and change the vote accordingly
        int newStatus = commentModel.getUserUpvoted() == 0 ? 1 : 0;

        // optimistic, update copy cached in adapter and make request to api to update in server
        commentModel.setUserUpvoted(newStatus);
        notifyItemChanged(itemPosition);
        delegate.onCommentVoted(commentModel, newStatus);
      });

      commentItemBinding.commentitemDownvote.setOnClickListener((View view) -> {
        CommentModel commentModel = commentList.get(itemPosition);
        // determine the new vote value based on the current one and change the vote accordingly
        int newStatus = commentModel.getUserUpvoted() == 0 ? -1 : 0;

        // optimistic, update copy cached in adapter and make request to api to update in server
        commentModel.setUserUpvoted(newStatus);
        notifyItemChanged(itemPosition);
        delegate.onCommentVoted(commentModel, newStatus);
      });
    }
  }

}
