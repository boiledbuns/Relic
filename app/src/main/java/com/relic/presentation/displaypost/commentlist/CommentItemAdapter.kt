package com.relic.presentation.displaypost.commentlist

import android.arch.lifecycle.LiveData
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.relic.R
import com.relic.data.models.CommentModel
import com.relic.presentation.displaypost.DisplayPostContract
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe

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
                return (
                        commentList[i].userUpvoted == newComments[i1].userUpvoted &&
                        commentList[i].body == newComments[i1].body &&
                        commentList[i].replyCount == newComments[i1].replyCount
                        )
            }
        }).dispatchUpdatesTo(this)

        commentList.clear()
        commentList.addAll(newComments)
    }

    private fun setReplies(newReplies: List<CommentModel>, itemPosition : Int) {
        commentList.apply {
            addAll(itemPosition + 1, newReplies)
            notifyItemRangeInserted(itemPosition + 1, newReplies.size)
        }
    }

    // region onclick handler

    fun displayCommentReplies(itemPosition : Int, commentExpanded : Boolean) {
        val comment = commentList[itemPosition].id
        if (commentExpanded) {
            // hide the comments if comments are currently expanded
        } else {
            actionDelegate.onExpandReply(comment).nonNull().observe {
                setReplies(it, itemPosition)
            }
        }

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