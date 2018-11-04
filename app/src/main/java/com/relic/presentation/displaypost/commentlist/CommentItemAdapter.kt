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
        return CommentItemVH(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.comment_item, parent, false)
        ).apply {
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
                return commentList[i].userUpvoted == newComments[i1].userUpvoted
            }
        }).dispatchUpdatesTo(this)

        commentList.clear()
        commentList.addAll(newComments)
    }

    // region onclick handler

    fun voteOnComment(itemPosition : Int, voteValue : Int) {
        actionDelegate.onCommentVoted(commentList[itemPosition], voteValue)
    }

    fun replyToComment(itemPosition : Int) {

    }

    // end region onclick handler
}