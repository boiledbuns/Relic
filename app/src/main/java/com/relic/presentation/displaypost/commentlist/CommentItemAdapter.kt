package com.relic.presentation.displaypost.commentlist

import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.relic.data.models.CommentModel
import com.relic.presentation.displaypost.DisplayPostContract

class CommentItemAdapter (
    private val actionDelegate : DisplayPostContract.PostViewDelegate
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var commentDiffer = AsyncListDiffer<CommentModel>(this, DIFF_CALLBACK)
    private val TAG = "COMMENT_ADAPTER"
    private val VIEW_TYPE_COMMENT = 0
    private val VIEW_TYPE_LOAD_MORE = 1

    override fun getItemCount(): Int = commentDiffer.currentList.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_COMMENT -> {
                (viewHolder as CommentItemVH).bindComment(commentDiffer.currentList[position], position)
            }
            VIEW_TYPE_LOAD_MORE -> {
                (viewHolder as CommentMoreItemsVH).bindLoadMore(commentDiffer.currentList[position], position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_COMMENT -> CommentItemVH(RelicCommentView(parent.context)).apply {
                initializeOnClicks(this@CommentItemAdapter)
            }
            else -> CommentMoreItemsVH(RelicCommentMoreItemsView(parent.context)).apply {
                initializeOnclicks(this@CommentItemAdapter)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (commentDiffer.currentList[position].isLoadMore) VIEW_TYPE_LOAD_MORE else VIEW_TYPE_COMMENT
    }

    fun setComments(newComments: List<CommentModel>) {
        commentDiffer.submitList(newComments)
    }

    // region onclick handler

    fun displayCommentReplies(itemId : String, commentExpanded : Boolean) {
        actionDelegate.onExpandReplies(itemId, commentExpanded)
    }

    fun voteOnComment(itemPosition : Int, voteValue : Int) {
        commentDiffer.currentList[itemPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newStatus = actionDelegate.onCommentVoted(it, voteValue)

            // optimistic, update copy cached in adapter and make request to api to update in server
            it.userUpvoted = newStatus
            notifyItemChanged(itemPosition)
        }
    }

    fun replyToComment(itemPosition : Int) {

    }

    // end region onclick handler

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CommentModel>() {
            override fun areItemsTheSame(p0: CommentModel, p1: CommentModel): Boolean {
                return p0.id == p1.id
            }

            override fun areContentsTheSame(p0: CommentModel, p1: CommentModel): Boolean {
                return (p0.userUpvoted == p1.userUpvoted &&
                    p0.replyCount == p1.replyCount)
            }
        }
    }
}