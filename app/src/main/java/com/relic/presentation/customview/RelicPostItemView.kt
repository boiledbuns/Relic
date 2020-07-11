package com.relic.presentation.customview

import android.content.Context
import android.graphics.PorterDuff
import android.text.Html
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.setPadding
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.interactor.Contract
import com.relic.interactor.PostInteraction
import com.relic.preference.POST_LAYOUT_CARD
import com.relic.presentation.base.ItemNotifier
import com.relic.presentation.helper.DateHelper
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_item_content.view.*
import kotlinx.android.synthetic.main.post_tags.view.*
import timber.log.Timber

class RelicPostItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    postLayout: Int
) : RelativeLayout(context, attrs, defStyleAttr) {
    // TODO need to think about increasing min api level from 21
    // TODO set color based on theme, still trying to figure out the best way to do this
    private val textColor: Int
    private val stickiedColor: Int
    private val backgroundColor: Int
    private val backgroundVisitedColor: Int

    private val defaultColor: Int
    private val downvotedColor: Int
    private val upvotedColor: Int

    private lateinit var post: PostModel

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

        defaultColor = context.resources.getColor(R.color.white)
        downvotedColor = context.resources.getColor(R.color.downvote)
        upvotedColor = context.resources.getColor(R.color.upvote)

        val layout = when (postLayout) {
            POST_LAYOUT_CARD -> R.layout.post_item_card
            else -> R.layout.post_item_span
        }
        LayoutInflater.from(context).inflate(layout, this, true)
    }

    fun setPost(postModel: PostModel) {
        post = postModel
        postItemRootView?.apply {
            updateVisited()
            updateSaveView()
            updateVoteView()

            val titleColor = if (postModel.stickied) stickiedColor else textColor
            titleView.setTextColor(titleColor)

            if (!postModel.thumbnail.isNullOrBlank()) {
                if (postModel.nsfw) {
                    postItemThumbnailView.apply {
                        setPadding(16)
                        setImageResource(R.drawable.ic_nsfw)
                    }
                } else {
                    postItemThumbnailView.setPadding(0)
                    setThumbnail(postModel.thumbnail!!)
                    domainTag.visibility = View.VISIBLE
                    domainTag.text = postModel.domain
                }

                postItemThumbnailView.visibility = View.VISIBLE
            } else {
                postItemThumbnailView.visibility = View.GONE
                domainTag.visibility = View.GONE
            }

            postSubNameView.text = resources.getString(R.string.sub_prefix_label, postModel.subreddit)
            postDateView.text = DateHelper.getDateDifferenceString(postModel.created!!)
            titleView.text = postModel.title
            postItemAuthorView.text = resources.getString(R.string.user_prefix_label, postModel.author)
            postItemTags.setPostTags(postModel)
            setAuthorFlair(postModel)

            if (!postModel.selftext.isNullOrEmpty()) {
                @Suppress("DEPRECATION")
                postBodyView.text = Html.fromHtml(postModel.selftext).toString()
                postBodyView.visibility = View.VISIBLE
            } else {
                postBodyView.visibility = View.GONE
            }

            postItemScore.text = postModel.score.toString()
            postItemCommentCountView.text = postModel.commentCount.toString()
        }
    }

    fun setViewDelegate(delegate: Contract.PostAdapterDelegate, notifier: ItemNotifier) {
        delegate.apply {
            setOnClickListener {
                interact(post, PostInteraction.Visit)
                notifier.notifyItem()
            }
            postItemSaveView.setOnClickListener {
                interact(post, PostInteraction.Save)
                notifier.notifyItem()
            }
            postItemUpvoteView.setOnClickListener {
                interact(post, PostInteraction.Upvote)
                notifier.notifyItem()
            }
            postItemDownvoteView.setOnClickListener {
                interact(post, PostInteraction.Downvote)
                notifier.notifyItem()
            }
            postItemThumbnailView.setOnClickListener {
                interact(post, PostInteraction.VisitLink)
                notifier.notifyItem()
            }
            postItemCommentView.setOnClickListener {
                interact(post, PostInteraction.NewReply)
            }
            postItemAuthorView.setOnClickListener {
                interact(post, PostInteraction.PreviewUser)
            }
        }
    }

    // region update view
    private fun updateVoteView() {
        when (post.userUpvoted) {
            1 -> {
                postItemUpvoteView.setColorFilter(upvotedColor)
                postItemDownvoteView.setColorFilter(defaultColor)
            }
            0 -> {
                postItemUpvoteView.setColorFilter(defaultColor)
                postItemDownvoteView.setColorFilter(defaultColor)
            }
            -1 -> {
                postItemUpvoteView.setColorFilter(defaultColor)
                postItemDownvoteView.setColorFilter(downvotedColor)
            }
        }
    }

    private fun updateSaveView() {
        // TODO convert all icons to use style colors
        val saveColor = if (post.saved) R.color.upvote else R.color.paleGray
        postItemSaveView.setColorFilter(resources.getColor(saveColor), PorterDuff.Mode.SRC_IN)
    }

    private fun updateVisited() {
        val backgroundColor = if (post.visited) backgroundVisitedColor else backgroundColor
        postItemRootView.setBackgroundColor(backgroundColor)
    }
    // endregion update view

    private fun setThumbnail(thumbnailUrl: String) {
        try {
            Timber.d("URL = $thumbnailUrl")
            Picasso.get().load(thumbnailUrl).fit().centerCrop().into(postItemThumbnailView)
            postItemThumbnailView.visibility = View.VISIBLE
        } catch (e: Error) {
            Timber.d("Issue loading image %s", e.toString())
        }
    }

    private fun setAuthorFlair(postModel: PostModel) {
        postItemAuthorFlairView.apply {
            if (postModel.authorFlair != null) {
                text = postModel.authorFlair
                // TODO replace with themes when adding proper theming
                @Suppress("DEPRECATION")
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }
}