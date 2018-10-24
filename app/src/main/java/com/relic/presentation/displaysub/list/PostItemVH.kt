package com.relic.presentation.displaysub.list

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.relic.R
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_item_span.view.*

class PostItemVH (
        itemView : RelicPostItem
) : RecyclerView.ViewHolder(itemView) {

    private var itemPosition  = 0

    fun initializeOnClicks(adapter : PostItemAdapter) {
        itemView.apply {
            itemView.setOnClickListener { adapter.onPostPressed(itemPosition) }
            itemView.savedPostIconView.setOnClickListener { adapter.onPostSavePressed(itemPosition) }
            itemView.postUpvoteView.setOnClickListener { adapter.onPostUpvotePressed(itemPosition) }
            itemView.postDownvoteView.setOnClickListener { adapter.onPostDownvotePressed(itemPosition) }
            itemView.postThumbnailView.setOnClickListener { adapter.onPostLinkPressed(itemPosition) }
            itemView.postCommentView.setOnClickListener { }
        }
    }

    fun bindPost(postModel : PostModel, position: Int) {
        itemView.apply {
            if (postModel.thumbnail.isNullOrBlank()) {
                postThumbnailView.visibility = View.GONE

            } else {
                postThumbnailView.visibility = View.VISIBLE
                postModel.thumbnail?.let{ setThumbnail(it) }
            }

            postSubNameView.text = resources.getString(R.string.sub_prefix_label, postModel.subreddit)
            postDateView.text = postModel.created
            titleView.text = postModel.title
            secondaryMetaTextview.text = postModel.author + " " + postModel.domain

            if (postModel.htmlSelfText.isNullOrEmpty()) {
                postBodyView.visibility = View.GONE
            }
            else {
                postBodyView.visibility = View.VISIBLE
                postBodyView.text = postModel.htmlSelfText
            }

            if (postModel.isVisited) {
                cardRootView.setBackgroundResource(R.color.backgroundSecondaryB)
            }
            else {
                cardRootView.setBackgroundResource(R.color.backgroundSecondary)
            }

            when (postModel.userUpvoted) {
                1 -> {
                    postUpvoteView.setImageResource(R.drawable.ic_upvote_active)
                    postDownvoteView.setImageResource(R.drawable.ic_downvote)
                }
                0 -> {
                    postUpvoteView.setImageResource(R.drawable.ic_upvote)
                    postDownvoteView.setImageResource(R.drawable.ic_downvote)
                }
                -1 -> {
                    postUpvoteView.setImageResource(R.drawable.ic_upvote)
                    postDownvoteView.setImageResource(R.drawable.ic_downvote_active)
                }
            }
        }

        itemPosition = position
    }

    private fun setThumbnail(thumbnailUrl : String) {
        try {
            Log.d("POSTITEM_ADAPTER", "URL = $thumbnailUrl")
            Picasso.get().load(thumbnailUrl).fit().centerCrop().into(itemView.postThumbnailView)
        } catch (e: Error) {
            Log.d("POSTITEM_ADAPTER", "Issue loading image " + e.toString())
        }
    }
}