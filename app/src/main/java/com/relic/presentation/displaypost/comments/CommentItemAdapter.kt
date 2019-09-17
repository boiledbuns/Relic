package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel
import com.relic.presentation.base.RelicAdapter
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.FullPostView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val VIEW_TYPE_POST = 0
private const val VIEW_TYPE_COMMENT = 1
private const val VIEW_TYPE_LOAD_MORE = 2

class CommentItemAdapter (
    private val actionDelegate : DisplayPostContract.PostViewDelegate
) : RelicAdapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>(), DisplayPostContract.CommentAdapterDelegate {

    private var post : PostModel? = null
    private var commentList : List<CommentModel> = ArrayList()

    private val TAG = "COMMENT_ADAPTER"

    private fun postSize() = if (post != null) 1 else 0
    override fun getItemCount(): Int = commentList.size + postSize()

    // helps us translate position in recyclerview to position in comment list because the actual
    // post and its comments are separate entities
    private fun getCommentPosition(adapterPosition : Int) : Int = adapterPosition - postSize()

    override fun onBindViewHolder(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_POST -> {
                (viewHolder as FullPostVH).bindPost(post!!)
            }
            VIEW_TYPE_COMMENT -> {
                (viewHolder as CommentItemVH).bindComment(commentList[position - postSize()])
            }
            VIEW_TYPE_LOAD_MORE -> {
                (viewHolder as CommentMoreItemsVH).bindLoadMore(commentList[position - postSize()])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POST -> FullPostVH(FullPostView(parent.context)).apply {
                initializeOnClicks(actionDelegate)
            }
            VIEW_TYPE_COMMENT -> CommentItemVH(RelicCommentView(parent.context)).apply {
                initializeOnClicks(this@CommentItemAdapter)
            }
            else -> CommentMoreItemsVH(RelicCommentMoreItemsView(parent.context)).apply {
                initializeOnClicks(this@CommentItemAdapter)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (post != null && position == 0) {
            VIEW_TYPE_POST
        }
        else {
            if (commentList[position - postSize()].isLoadMore) VIEW_TYPE_LOAD_MORE else VIEW_TYPE_COMMENT
        }
    }

    fun setPost(postModel: PostModel) {
        if (post == null) {
            post = postModel
            notifyDataSetChanged()
        } else {
            post = postModel
            notifyDataSetChanged()
        }
    }

    fun setComments(newComments: List<CommentModel>, onPostsCalculated : () -> Unit) {
        launch {
            val diffResult = calculateCommentDiffs(newComments)
            withContext(Dispatchers.Main) {
                onPostsCalculated()
                diffResult.dispatchUpdatesTo(this@CommentItemAdapter)
            }

            commentList = newComments
        }
    }

    private fun calculateCommentDiffs(newComments: List<CommentModel>) : DiffUtil.DiffResult{
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return commentList.size + postSize()
            }

            override fun getNewListSize(): Int {
                return newComments.size + postSize()
            }

            override fun areItemsTheSame(i: Int, i1: Int): Boolean {
                val newP = i1 - postSize()
                val oldP = i - postSize()

                val new = if (newP < 0) post!! else newComments[newP]
                val old = if (oldP < 0) post!! else commentList[oldP]

                return new.fullName == old.fullName
            }

            override fun areContentsTheSame(i: Int, i1: Int): Boolean {
                val newP = i1 - postSize()
                val oldP = i - postSize()

                val new = if (newP < 0) post!! else newComments[newP]
                val old = if (oldP < 0) post!! else commentList[oldP]

                return when(new) {
                    is PostModel -> {
                        if (old is PostModel) {
                            old.selftext == new.selftext
                        } else false
                    }
                    is CommentModel -> {
                        if (old is CommentModel) {
                            old.userUpvoted == new.userUpvoted &&
                            old.body == new.body &&
                            old.replyCount == new.replyCount
                        } else false
                    }
                    else -> false
                }


            }
        })
    }

    // region OnClick handlers

    override fun voteOnComment(itemPosition : Int, voteValue : Int) {
        val commentPosition = getCommentPosition(itemPosition)
        commentList[commentPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newStatus = actionDelegate.onCommentVoted(it as CommentModel, voteValue)

            // optimistic, update copy cached in adapter and make request to api to update in server
            it.userUpvoted = newStatus
            notifyDataSetChanged()
        }
    }

    override fun replyToComment(itemPosition : Int, text: String) {
        val commentPosition = getCommentPosition(itemPosition)
        actionDelegate.onReplyPressed(commentList[commentPosition].fullName, text)
    }

    override fun visitComment(itemPosition: Int) {}

    override fun previewUser(itemPosition: Int) {
        val commentPosition = getCommentPosition(itemPosition)
        actionDelegate.onUserPressed(commentList[commentPosition])
    }

    override fun loadMoreComments(itemPosition: Int, displayReplies : Boolean) {
        val commentPosition = getCommentPosition(itemPosition)
        actionDelegate.onExpandReplies(commentList[commentPosition], displayReplies)
    }

    // endregion OnClick handlers
}