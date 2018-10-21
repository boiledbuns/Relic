package com.relic.presentation.displaysub.list

import android.support.v7.widget.RecyclerView
import android.view.View
import com.relic.R
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItem
import kotlinx.android.synthetic.main.post_item_span.view.*

class PostItemVH (
    itemView : RelicPostItem
) : RecyclerView.ViewHolder(itemView) {

    fun bindPost(postModel : PostModel) {
        itemView.apply {
            primaryMetaTextview.text = resources.getString(R.string.sub_prefix_name, postModel.subreddit) + " " + postModel.created
            titleView.text = postModel.title
            secondaryMetaTextview.text = postModel.author + " " + postModel.domain

            postModel.htmlSelfText?.let {
                postBodyView.visibility = View.VISIBLE
                postBodyView.text = it
            }

            if (postModel.isVisited) {
                setBackgroundColor(resources.getColor(R.color.backgroundSecondaryB))
            }
        }
    }
}