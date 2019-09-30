package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.CommentModel
import com.relic.presentation.displaypost.DisplayPostContract

class CommentMoreItemsVH (
    private val moreCommentsItem : RelicCommentMoreItemsView
) : RecyclerView.ViewHolder(moreCommentsItem) {

    fun initializeOnClicks(delegate : DisplayPostContract.CommentAdapterDelegate) {
        moreCommentsItem.setOnClickListener {
            delegate.loadMoreComments(adapterPosition, false)
            // TODO display loading for comments
        }
    }

    fun bindLoadMore(commentModel : CommentModel){
        commentModel.more?.let { more ->
            if (more.isNotEmpty()) moreCommentsItem.displayLoadMore(more.size)
        }

        moreCommentsItem.displayReplyDepth(commentModel.depth)
    }
}