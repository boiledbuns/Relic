package com.relic.presentation.displaysub.list

import android.support.v7.widget.RecyclerView
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItemView
import kotlinx.android.synthetic.main.post_item_span.view.*

class PostItemVH (
    private val postItemView : RelicPostItemView
) : RecyclerView.ViewHolder(postItemView) {

    private var itemPosition = 0
    var itemFullName = ""

    fun initializeOnClicks(adapter : PostItemAdapter) {
        itemView.apply {
            setOnClickListener { adapter.onPostPressed(itemPosition) }
            savedPostIconView.setOnClickListener { adapter.onPostSavePressed(itemPosition) }
            postUpvoteView.setOnClickListener { adapter.onPostUpvotePressed(itemPosition) }
            postDownvoteView.setOnClickListener { adapter.onPostDownvotePressed(itemPosition) }
            postThumbnailView.setOnClickListener { adapter.onPostLinkPressed(itemPosition) }
            postCommentView.setOnClickListener { }
        }
    }

    fun bindPost(postModel : PostModel, position: Int) {
        postItemView.setPost(postModel)

        itemPosition = position
        itemFullName = postModel.id
    }
}