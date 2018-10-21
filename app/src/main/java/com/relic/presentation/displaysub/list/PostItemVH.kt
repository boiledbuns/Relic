package com.relic.presentation.displaysub.list

import android.support.v7.widget.RecyclerView
import android.view.View
import com.relic.R
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItem
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.android.synthetic.main.post_item_span.view.*

class PostItemVH (
        private val postAdapterDelegate : DisplaySubContract.PostAdapterDelegate,
        itemView : RelicPostItem
) : RecyclerView.ViewHolder(itemView) {

    private var itemPosition  = 0

    fun bindPost(postModel : PostModel, position: Int) {
        itemView.apply {
            primaryMetaTextview.text = "[ " + resources.getString(R.string.sub_prefix_name, postModel.subreddit) + " ] " + postModel.created
            titleView.text = postModel.title
            secondaryMetaTextview.text = postModel.author + " " + postModel.domain

            postModel.htmlSelfText?.let {
                postBodyView.visibility = View.VISIBLE
                postBodyView.text = it
            }

            if (postModel.isVisited) {
                setBackgroundColor(resources.getColor(R.color.backgroundSecondaryB))
            }

            when (postModel.userUpvoted) {
                1 -> postUpvoteView.setImageResource(R.drawable.ic_upvote_active)
                -1 -> postUpvoteView.setImageResource(R.drawable.ic_downvote_active)
            }
        }

        itemPosition = position
    }

    fun initializeOnClicks(adapter : PostItemAdapter) {
        itemView.apply {
            itemView.setOnClickListener { adapter.onPostPressed(itemPosition) }
            itemView.savedPostIconView.setOnClickListener { adapter.onPostSavePressed(itemPosition) }
            itemView.postUpvoteView.setOnClickListener { adapter.onPostUpvotePressed(itemPosition) }
            itemView.postDownvoteView.setOnClickListener { adapter.onPostDownvotePressed(itemPosition) }
            itemView.postCommentView.setOnClickListener { }
        }
    }
}