package com.relic.presentation.displaypost.commentlist

import android.support.v7.widget.RecyclerView
import com.relic.data.models.CommentModel
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
        moreCommentsItem.displayReplyDepth(commentModel.depth)
    }
}