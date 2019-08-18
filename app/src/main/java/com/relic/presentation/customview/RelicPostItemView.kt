package com.relic.presentation.customview

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.preference.POST_LAYOUT_CARD
import com.relic.presentation.helper.DateHelper
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_item_content.view.*
import kotlinx.android.synthetic.main.post_tags.view.*
import timber.log.Timber

class RelicPostItemView @JvmOverloads constructor(
        context: Context,
        attrs : AttributeSet? = null,
        defStyleAttr : Int = 0,
        postLayout : Int
) : RelativeLayout(context, attrs, defStyleAttr) {
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

        val layout = when (postLayout) {
            POST_LAYOUT_CARD -> R.layout.post_item_card
            else -> R.layout.post_item_span
        }
        LayoutInflater.from(context).inflate(layout, this, true)
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
                domainTag.visibility = View.VISIBLE
                domainTag.text = postModel.domain
            } else {
                postItemThumbnailView.visibility = View.GONE
                domainTag.visibility = View.GONE
            }

            postSubNameView.text = resources.getString(R.string.sub_prefix_label, postModel.subreddit)
            postModel.created?.let { postDateView.text = DateHelper.getDateDifferenceString(it) }
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
        nsfwTag.visibility = if (postModel.nsfw) View.VISIBLE else View.GONE

        postTag.apply {
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