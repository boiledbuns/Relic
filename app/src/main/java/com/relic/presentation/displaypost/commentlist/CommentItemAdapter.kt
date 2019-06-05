package com.relic.presentation.displaypost.commentlist

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import com.relic.domain.models.CommentModel
import com.relic.presentation.displaypost.DisplayPostContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentItemAdapter (
    private val actionDelegate : DisplayPostContract.PostViewDelegate
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), DisplayPostContract.CommentAdapterDelegate {

    private var commentList : List<CommentModel> = ArrayList()

    private val TAG = "COMMENT_ADAPTER"
    private val VIEW_TYPE_COMMENT = 0
    private val VIEW_TYPE_LOAD_MORE = 1

    override fun getItemCount(): Int = commentList.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_COMMENT -> {
                (viewHolder as CommentItemVH).bindComment(commentList[position])
            }
            VIEW_TYPE_LOAD_MORE -> {
                (viewHolder as CommentMoreItemsVH).bindLoadMore(commentList[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_COMMENT -> CommentItemVH(RelicCommentView(parent.context)).apply {
                initializeOnClicks(this@CommentItemAdapter)
            }
            else -> CommentMoreItemsVH(RelicCommentMoreItemsView(parent.context)).apply {
                initializeOnClicks(this@CommentItemAdapter)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (commentList[position].isLoadMore) VIEW_TYPE_LOAD_MORE else VIEW_TYPE_COMMENT
    }

    suspend fun setComments(newComments: List<CommentModel>, onPostsCalculated : () -> Unit) {
        if (commentList.isEmpty()) {
            commentList = newComments
            notifyDataSetChanged()
        } else {
            withContext(Dispatchers.Default) {
                calculateDiffs(newComments)
            }.dispatchUpdatesTo(this)
            commentList = newComments
        }
    }

    private fun calculateDiffs(newComments: List<CommentModel>) : DiffUtil.DiffResult{
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return commentList.size
            }

            override fun getNewListSize(): Int {
                return newComments.size
            }

            override fun areItemsTheSame(i: Int, i1: Int): Boolean {
                return commentList[i].fullName == newComments[i1].fullName
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
        })
    }

    // region OnClick handlers

    override fun displayCommentReplies(itemId : String, commentExpanded : Boolean) {
        actionDelegate.onExpandReplies(itemId, commentExpanded)
    }

    override fun voteOnComment(itemPosition : Int, voteValue : Int) {
        commentList[itemPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newStatus = actionDelegate.onCommentVoted(it, voteValue)

            // optimistic, update copy cached in adapter and make request to api to update in server
            it.userUpvoted = newStatus
            notifyItemChanged(itemPosition)
        }
    }

    override fun replyToComment(itemPosition : Int, text: String) {
        actionDelegate.onReplyPressed(commentList[itemPosition].fullName, text)
    }

    override fun visitComment(itemPosition: Int) {}

    override fun previewUser(itemPosition: Int) {
        actionDelegate.onUserPressed(commentList[itemPosition])
    }

    override fun loadMoreComments(itemPosition: Int, displayReplies : Boolean) {
        actionDelegate.onExpandReplies(commentList[itemPosition].fullName, displayReplies)
    }

    // endregion OnClick handlers
}