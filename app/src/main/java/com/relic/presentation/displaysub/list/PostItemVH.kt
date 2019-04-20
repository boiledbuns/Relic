package com.relic.presentation.displaysub.list

import android.support.v7.widget.RecyclerView
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.android.synthetic.main.post_item_span.view.*

class PostItemVH (
    private val postItemView : RelicPostItemView
) : RecyclerView.ViewHolder(postItemView) {

    var itemFullName = ""

    fun initializeOnClicks(adapter : DisplaySubContract.PostItemAdapterDelegate) {
        itemView.apply {
            setOnClickListener { adapter.onPostPressed(adapterPosition) }
            savedPostIconView.setOnClickListener { adapter.onPostSavePressed(adapterPosition) }
            postUpvoteView.setOnClickListener { adapter.onPostUpvotePressed(adapterPosition) }
            postDownvoteView.setOnClickListener { adapter.onPostDownvotePressed(adapterPosition) }
            postThumbnailView.setOnClickListener { adapter.onPostLinkPressed(adapterPosition) }
            postCommentView.setOnClickListener { }
        }
    }

    fun bindPost(postModel : PostModel) {
        postItemView.setPost(postModel)
        itemFullName = postModel.fullName
    }
}