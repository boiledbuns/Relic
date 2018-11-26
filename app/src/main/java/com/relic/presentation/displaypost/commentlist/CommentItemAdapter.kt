package com.relic.presentation.displaypost.commentlist

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.relic.R
import com.relic.data.models.CommentModel
import com.relic.presentation.displaypost.DisplayPostContract

class CommentItemAdapter (
    private val actionDelegate : DisplayPostContract.PostViewDelegate
) : RecyclerView.Adapter<CommentItemVH>() {

    private val TAG = "COMMENT_ADAPTER"
    private var commentList : MutableList<CommentModel> = ArrayList()

    override fun getItemCount(): Int = commentList.size

    override fun onBindViewHolder(viewHolder: CommentItemVH, position: Int) {
        viewHolder.bindComment(commentList[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): CommentItemVH {
        return CommentItemVH(CommentView(parent.context)).apply {
            initializeOnClicks(this@CommentItemAdapter)
        }
    }

    fun setComments(newComments: List<CommentModel>) {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return commentList.size
            }

            override fun getNewListSize(): Int {
                return newComments.size
            }

            override fun areItemsTheSame(i: Int, i1: Int): Boolean {
                return commentList[i].id == newComments[i1].id
            }

            override fun areContentsTheSame(i: Int, i1: Int): Boolean {
                val oldComment = commentList[i]
                val newComment = newComments[i1]
                return (
                    oldComment.userUpvoted == newComment.userUpvoted &&
                    oldComment.body == newComment.body &&
                    oldComment.replyCount == newComment.replyCount
                )
            }
        }).dispatchUpdatesTo(this)

        commentList.clear()
        commentList.addAll(newComments)
    }

    // region onclick handler

    fun displayCommentReplies(itemId : String, commentExpanded : Boolean) {
        actionDelegate.onExpandReplies(itemId, commentExpanded)
    }

    fun voteOnComment(itemPosition : Int, voteValue : Int) {
        commentList[itemPosition].also {
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
}