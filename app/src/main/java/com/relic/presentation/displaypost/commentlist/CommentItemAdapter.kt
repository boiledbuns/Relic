package com.relic.presentation.displaypost.commentlist

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
        if (commentList.size > 0) {
            commentList.apply {
                addAll(itemPosition + 1, newReplies)
                notifyItemRangeInserted(itemPosition + 1, newReplies.size)
            }
        }
        else {
            // alert user of failure
        }
    }

    private fun hideReplies(itemPosition: Int) {
        val parentDepth = commentList[itemPosition].depth
        var itemsToRemove = 0

        // hide anything with depth > item.depth until reaching another item of the same depth
        var comparePosition = itemPosition + 1
        while (comparePosition < commentList.size && commentList[comparePosition].depth > parentDepth) {
            // add up number of items to be removed
            itemsToRemove ++
            comparePosition ++
        }

        commentList.subList(itemPosition + 1, itemPosition + 1 + itemsToRemove).clear()
        notifyItemRangeRemoved(itemPosition + 1, itemPosition + 1 + itemsToRemove)
    }

    // region onclick handler

    fun displayCommentReplies(itemPosition : Int, commentExpanded : Boolean) {
        if (commentExpanded) {
            hideReplies(itemPosition)
        } else {
            actionDelegate.onExpandReply(commentList[itemPosition].id).nonNull().observe {
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