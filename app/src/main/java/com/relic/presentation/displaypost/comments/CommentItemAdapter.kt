package com.relic.presentation.displaypost.comments

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel
import com.relic.interactor.Contract
import com.relic.presentation.base.ItemNotifier
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
    private val delegate : DisplayPostContract.LoadMoreCommentsDelegate,
    private val commentInteractor : Contract.CommentAdapterDelegate,
    private val postInteractor : Contract.PostAdapterDelegate
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
    ) : RecyclerView.ViewHolder(fullPostView) {

        init {
            fullPostView.setViewDelegate(postInteractor)
        }

        fun bind(postModel: PostModel) {
            fullPostView.setPost(postModel)
        }
    }

    private inner class CommentItemVH(
      private val view : View,
      private val viewType : Int
    ) : RecyclerView.ViewHolder(view), ItemNotifier {


        init {
            when(viewType) {
                VIEW_TYPE_COMMENT -> (view as RelicCommentView).setViewDelegate(commentInteractor, this)
                VIEW_TYPE_LOAD_MORE -> (view as RelicCommentMoreItemsView).setViewDelegate(delegate, this)
            }
        }

        fun bind(commentModel : CommentModel) {
            when(viewType) {
                VIEW_TYPE_COMMENT -> (view as RelicCommentView).setComment(commentModel)
                VIEW_TYPE_LOAD_MORE -> (view as RelicCommentMoreItemsView).setLoadMore(commentModel)
            }
        }

        override fun notifyItem() {
            notifyItemChanged(getCommentPosition(layoutPosition))
        }
    }

    // endregion OnClick handlers
}