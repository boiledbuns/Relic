package com.relic.presentation.displaysub.list

import android.support.v7.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.android.synthetic.main.post_item_content.view.*

class PostItemVH (
    val postItemView : RelicPostItemView
) : RecyclerView.ViewHolder(postItemView) {

    var itemFullName = ""

    fun initializeOnClicks(adapter : DisplaySubContract.PostItemAdapterDelegate) {
        itemView.apply {
            setOnClickListener { adapter.onPostPressed(adapterPosition) }
            savedPostIconView.setOnClickListener { adapter.onPostSavePressed(adapterPosition) }
            postItemUpvoteView.setOnClickListener { adapter.onPostUpvotePressed(adapterPosition) }
            postItemDownvoteView.setOnClickListener { adapter.onPostDownvotePressed(adapterPosition) }
            postItemThumbnailView.setOnClickListener { adapter.onPostLinkPressed(adapterPosition) }
            postItemCommentView.setOnClickListener { }
            postItemAuthorView.setOnClickListener { adapter.onUserPressed(adapterPosition) }
        }
    }

    fun bindPost(postModel : PostModel) {
        postItemView.setPost(postModel)
        itemFullName = postModel.fullName
    }
}