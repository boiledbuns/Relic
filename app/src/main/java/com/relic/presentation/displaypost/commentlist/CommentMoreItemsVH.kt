package com.relic.presentation.displaypost.commentlist

import android.support.v7.widget.RecyclerView
import com.relic.data.models.CommentModel

class CommentMoreItemsVH (
    val moreCommentsItem : RelicCommentMoreItemsView
) : RecyclerView.ViewHolder(moreCommentsItem) {

    fun initializeOnclicks(adapter : CommentItemAdapter) {
        // TODO once comment reply retrieval is completed
    }

    fun bindLoadMore(commentModel : CommentModel, position : Int){
        moreCommentsItem.displayReplyDepth(commentModel.depth)
    }
}