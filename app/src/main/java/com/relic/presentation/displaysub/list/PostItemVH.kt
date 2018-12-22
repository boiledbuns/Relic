package com.relic.presentation.displaysub.list

import android.graphics.Color
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
        itemView.apply {
            val backgroundColor = if (postModel.isVisited) {
                R.color.backgroundSecondaryB
            } else R.color.backgroundSecondary

            postItemRootView.setBackgroundColor(resources.getColor(backgroundColor))

            if (!postModel.thumbnail.isNullOrBlank()) {
                postThumbnailView.visibility = View.VISIBLE
                setThumbnail(postModel.thumbnail)
            } else {
                postThumbnailView.visibility = View.GONE
            }

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
        itemFullName = postModel.id
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
        itemView.postItemNSFWView.visibility = if (postModel.isNsfw) View.VISIBLE else View.GONE

        itemView.postItemTagView.apply {
            if (postModel.linkFlair != null) {
                text = postModel.linkFlair
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            } else { visibility = View.GONE }
        }

        itemView.postItemAuthorFlairView.apply {
            if (postModel.authorFlair != null) {
                text = postModel.authorFlair
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            } else { visibility = View.GONE }
        }
    }
}