package com.relic.presentation.displaypost.comments

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel
import com.relic.presentation.base.RelicAdapter
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.FullPostView
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val VIEW_TYPE_POST = 0
private const val VIEW_TYPE_COMMENT = 1
private const val VIEW_TYPE_LOAD_MORE = 2

class CommentItemAdapter (
    private val delegate : DisplayPostContract.ViewModel,
    private val commentInteractor : DisplayPostContract.CommentAdapterDelegate,
    private val postInteractor : DisplaySubContract.PostAdapterDelegate
) : RelicAdapter<RecyclerView.ViewHolder>() {

    var post : PostModel? = null
    private var commentList : List<CommentModel> = ArrayList()

    private fun postSize() = if (post != null) 1 else 0
    override fun getItemCount(): Int = commentList.size + postSize()

    // helps us translate position in recyclerview to position in comment list because the actual
    // post and its comments are separate entities
    private fun getCommentPosition(layoutPosition : Int) : Int = layoutPosition - postSize()

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_POST -> {
                (viewHolder as FullPostVH).bind(post!!)
            }
            VIEW_TYPE_COMMENT -> {
                (viewHolder as CommentItemVH).bind(commentList[position - postSize()])
            }
            VIEW_TYPE_LOAD_MORE -> {
                (viewHolder as CommentItemVH).bind(commentList[position - postSize()])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POST -> FullPostVH(FullPostView(parent.context))
            VIEW_TYPE_COMMENT -> CommentItemVH(RelicCommentView(parent.context), viewType)
            else -> CommentItemVH(RelicCommentMoreItemsView(parent.context), viewType)
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

    private inner class FullPostVH(
      private val fullPostView : FullPostView
    ) : RecyclerView.ViewHolder(fullPostView), DisplaySubContract.PostViewDelegate {

        init { fullPostView.setViewDelegate(this) }

        fun bind(postModel: PostModel) {
            fullPostView.setPost(postModel)
        }

        override fun onPostPressed() = postInteractor.visitPost(getPost())
        override fun onPostSavePressed() = postInteractor.savePost(getPost(), !getPost().saved)
        override fun onPostUpvotePressed() = postInteractor.voteOnPost(getPost(), UPVOTE_PRESSED)
        override fun onPostDownvotePressed() = postInteractor.voteOnPost(getPost(), DOWNVOTE_PRESSED)
        override fun onPostReply() = postInteractor.onNewReplyPressed(getPost())
        override fun onPostLinkPressed() = postInteractor.onLinkPressed(getPost())
        override fun onUserPressed() = postInteractor.previewUser(getPost())

        fun getPost() = post!!
    }

    private inner class CommentItemVH(
      private val view : View,
      private val viewType : Int
    ) : RecyclerView.ViewHolder(view), DisplayPostContract.CommentViewDelegate {

        init {
            when(viewType) {
                VIEW_TYPE_COMMENT -> (view as RelicCommentView).setViewDelegate(this@CommentItemVH)
                VIEW_TYPE_LOAD_MORE -> (view as RelicCommentMoreItemsView).setViewDelegate(this@CommentItemVH)
            }
        }

        fun bind(commentModel : CommentModel) {
            when(viewType) {
                VIEW_TYPE_COMMENT -> (view as RelicCommentView).setComment(commentModel)
                VIEW_TYPE_LOAD_MORE -> (view as RelicCommentMoreItemsView).setLoadMore(commentModel.replyCount)
            }
        }

        override fun voteOnComment(voteValue: Int) = commentInteractor.onCommentVoted(getComment(layoutPosition), voteValue)
        override fun replyToComment(text: String) = commentInteractor.onReplyPressed(getComment(layoutPosition), text)
        override fun previewUser() = commentInteractor.onPreviewUser(getComment(layoutPosition))
        override fun loadMoreComments(displayReplies: Boolean) = commentInteractor.onExpandReplies(getComment(layoutPosition))
        override fun visitComment() {}

        fun getComment(commentLayoutPosition: Int) = commentList[getCommentPosition(commentLayoutPosition)]
    }

    // region OnClick handlers
//    override fun voteOnComment(itemPosition : Int, voteValue : Int) {
//        val commentPosition = getCommentPosition(itemPosition)
//        commentList[commentPosition].also {
//            // determine the new vote value based on the current one and change the vote accordingly
//            val newStatus = commentInteractor.onCommentVoted(it, voteValue)
//
//            // optimistic, update copy cached in adapter and make request to api to update in server
//            it.userUpvoted = newStatus
//            notifyDataSetChanged()
//        }
//    }
//
//    override fun replyToComment(itemPosition : Int, text: String) {
//        val commentPosition = getCommentPosition(itemPosition)
//        commentInteractor.onReplyPressed(commentList[commentPosition].fullName, text)
//    }
//
//    override fun visitComment(itemPosition: Int) {}
//
//    override fun previewUser(itemPosition: Int) {
//        val commentPosition = getCommentPosition(itemPosition)
//        postInteractor.previewUser(commentList[commentPosition].author)
//    }
//
//    override fun loadMoreComments(itemPosition: Int, displayReplies : Boolean) {
//        val commentPosition = getCommentPosition(itemPosition)
//        delegate.onExpandReplies(commentList[commentPosition], displayReplies)
//    }

    // endregion OnClick handlers
}