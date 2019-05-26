package com.relic.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.relic.R
import com.relic.data.models.PostModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_item_span.view.*
import ru.noties.markwon.Markwon

class RelicPostItemView @JvmOverloads constructor(
        context: Context,
        attrs : AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val TAG = "POST_ITEM_VIEW"
    // TODO need to think about increasing min api level from 21
    // TODO set color based on theme, still trying to figure out the best way to do this
    private val textColor: Int
    private val stickiedColor: Int
    private val backgroundColor: Int
    private val backgroundVisitedColor: Int

    init {
        val typedVal = TypedValue()

        context.theme.apply {
            resolveAttribute(R.attr.colorPrimary, typedVal, true)
            backgroundColor = typedVal.data

            resolveAttribute(R.attr.relicBackgroundColorB, typedVal, true)
            backgroundVisitedColor = typedVal.data

            resolveAttribute(android.R.attr.textColor, typedVal, true)
            textColor = typedVal.data

            resolveAttribute(R.attr.relicStickiedColor, typedVal, true)
            stickiedColor = typedVal.data
        }

        LayoutInflater.from(context).inflate(R.layout.post_item_span, this, true)
    }

    fun setPost(postModel : PostModel) {
        postItemRootView?.apply {
            if (postModel.isVisited) {
                setBackgroundColor(backgroundVisitedColor)
            } else {
                setBackgroundColor(backgroundColor)
            }

            if (postModel.isStickied) {
                titleView.setTextColor(stickiedColor)
            } else {
                titleView.setTextColor(textColor)
            }

            if (!postModel.thumbnail.isNullOrBlank()) {
                postItemThumbnailView.visibility = View.VISIBLE
                setThumbnail(postModel.thumbnail)
                postItemLinkDomain.visibility = View.VISIBLE
                postItemLinkDomain.text = postModel.domain
            } else {
                postItemThumbnailView.visibility = View.GONE
                postItemLinkDomain.visibility = View.GONE
            }

            postSubNameView.text = resources.getString(R.string.sub_prefix_label, postModel.subreddit)
            postDateView.text = postModel.created
            titleView.text = postModel.title
            postItemAuthorView.text = resources.getString(R.string.user_prefix_label, postModel.author)
            setPostTags(postModel)

            if (!postModel.htmlSelfText.isNullOrEmpty()) {
                postBodyView.text = postModel.htmlSelfText
                postBodyView.visibility = View.VISIBLE
            }
            else {
                postBodyView.visibility = View.GONE
            }

            setVote(postModel.userUpvoted)

            postItemScore.text = postModel.score.toString()
            postItemCommentCountView.text = postModel.commentCount.toString()
        }
    }

    fun setVote(vote : Int) {
        when (vote) {
            1 -> {
                postItemUpvoteView.setImageResource(R.drawable.ic_upvote_active)
                postItemDownvoteView.setImageResource(R.drawable.ic_downvote)
            }
            0 -> {
                postItemUpvoteView.setImageResource(R.drawable.ic_upvote)
                postItemDownvoteView.setImageResource(R.drawable.ic_downvote)
            }
            -1 -> {
                postItemUpvoteView.setImageResource(R.drawable.ic_upvote)
                postItemDownvoteView.setImageResource(R.drawable.ic_downvote_active)
            }
        }
    }

    private fun setThumbnail(thumbnailUrl : String) {
        try {
            Log.d(TAG, "URL = $thumbnailUrl")
            Picasso.get().load(thumbnailUrl).fit().centerCrop().into(postItemThumbnailView)
            postItemThumbnailView.visibility = View.VISIBLE
        } catch (e: Error) {
            Log.d(TAG, "Issue loading image " + e.toString())
        }
    }

    private fun setPostTags(postModel: PostModel) {
        //secondaryMetaTextview.text = resources.getString(R.string.user_prefix_label, postModel.author + " " + postModel.domain + " " + postModel.linkFlair)
        postItemNSFWView.visibility = if (postModel.isNsfw) View.VISIBLE else View.GONE

        postItemTagView.apply {
            if (postModel.linkFlair != null) {
                text = postModel.linkFlair
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            } else { visibility = View.GONE }
        }

        postItemAuthorFlairView.apply {
            if (postModel.authorFlair != null) {
                text = postModel.authorFlair
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            } else { visibility = View.GONE }
        }
    }

}