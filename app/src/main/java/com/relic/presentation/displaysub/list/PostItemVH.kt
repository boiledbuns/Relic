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
            setOnClickListener { adapter.onPostPressed(itemPosition) }
            savedPostIconView.setOnClickListener { adapter.onPostSavePressed(itemPosition) }
            postUpvoteView.setOnClickListener { adapter.onPostUpvotePressed(itemPosition) }
            postDownvoteView.setOnClickListener { adapter.onPostDownvotePressed(itemPosition) }
            postThumbnailView.setOnClickListener { adapter.onPostLinkPressed(itemPosition) }
            postCommentView.setOnClickListener { }
        }
    }

    fun bindPost(postModel : PostModel, position: Int) {
        itemView.apply {
            if (!postModel.thumbnail.isNullOrBlank()) setThumbnail(postModel.thumbnail)

            postSubNameView.text = resources.getString(R.string.sub_prefix_label, postModel.subreddit)
            postDateView.text = postModel.created
            titleView.text = postModel.title
            setPostTags(postModel)

            if (!postModel.htmlSelfText.isNullOrEmpty()) {
                postBodyView.text = postModel.htmlSelfText
                postBodyView.visibility = View.VISIBLE
            }
            else {
                postBodyView.visibility = View.GONE
            }

            cardRootView.setBackgroundResource(if (postModel.isVisited)
                R.color.backgroundSecondary else R.color.backgroundSecondary)

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

            postScore.text = postModel.score.toString()
            postCommentCountView.text = postModel.commentCount.toString()
        }
        itemPosition = position
    }

    private fun setThumbnail(thumbnailUrl : String) {
        try {
            Log.d("POSTITEM_ADAPTER", "URL = $thumbnailUrl")
            Picasso.get().load(thumbnailUrl).fit().centerCrop().into(itemView.postThumbnailView)
            itemView.postThumbnailView.visibility = View.VISIBLE
        } catch (e: Error) {
            Log.d("POSTITEM_ADAPTER", "Issue loading image " + e.toString())
        }
    }

    private fun setPostTags(postModel: PostModel) {
        //secondaryMetaTextview.text = resources.getString(R.string.user_prefix_label, postModel.author + " " + postModel.domain + " " + postModel.linkFlair)
        itemView.secondaryMetaTextview.apply {
            postModel.linkFlair?.let {
                text = it
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            }
        }
    }
}