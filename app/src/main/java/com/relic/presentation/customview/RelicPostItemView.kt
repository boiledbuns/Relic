package com.relic.presentation.customview

import android.content.Context
import android.support.v4.text.HtmlCompat
import android.text.Html
import android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.PostModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_item_span.view.*
import timber.log.Timber

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
            if (postModel.visited) {
                setBackgroundColor(backgroundVisitedColor)
            } else {
                setBackgroundColor(backgroundColor)
            }

            if (postModel.stickied) {
                titleView.setTextColor(stickiedColor)
            } else {
                titleView.setTextColor(textColor)
            }

            if (!postModel.thumbnail.isNullOrBlank()) {
                val thumbnail = postModel.thumbnail!!
                postItemThumbnailView.visibility = View.VISIBLE
                setThumbnail(thumbnail)
                postItemLinkDomain.visibility = View.VISIBLE
                postItemLinkDomain.text = postModel.domain
            } else {
                postItemThumbnailView.visibility = View.GONE
                postItemLinkDomain.visibility = View.GONE
            }

            postSubNameView.text = resources.getString(R.string.sub_prefix_label, postModel.subreddit)
            postDateView.text = postModel.created.toString()
            titleView.text = postModel.title
            postItemAuthorView.text = resources.getString(R.string.user_prefix_label, postModel.author)
            setPostTags(postModel)

            if (!postModel.selftext.isNullOrEmpty()) {
                @Suppress("DEPRECATION")
                postBodyView.text = Html.fromHtml(postModel.selftext).toString()
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
            Timber.d( "URL = $thumbnailUrl")
            Picasso.get().load(thumbnailUrl).fit().centerCrop().into(postItemThumbnailView)
            postItemThumbnailView.visibility = View.VISIBLE
        } catch (e: Error) {
            Timber.d("Issue loading image %s", e.toString())
        }
    }

    private fun setPostTags(postModel: PostModel) {
        //secondaryMetaTextview.text = resources.getString(R.string.user_prefix_label, postModel.author + " " + postModel.domain + " " + postModel.linkFlair)
        postItemNSFWView.visibility = if (postModel.nsfw) View.VISIBLE else View.GONE

        postItemTagView.apply {
            if (postModel.linkFlair != null) {
                text = postModel.linkFlair
                // TODO replace with themes when adding proper theming
                @Suppress("DEPRECATION")
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            } else { visibility = View.GONE }
        }

        postItemAuthorFlairView.apply {
            if (postModel.authorFlair != null) {
                text = postModel.authorFlair
                // TODO replace with themes when adding proper theming
                @Suppress("DEPRECATION")
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            } else { visibility = View.GONE }
        }
    }

}