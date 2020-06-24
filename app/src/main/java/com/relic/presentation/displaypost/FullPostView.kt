package com.relic.presentation.displaypost

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.interactor.Contract
import com.relic.interactor.PostInteraction
import com.relic.presentation.helper.DateHelper
import com.relic.presentation.util.MediaHelper
import com.relic.presentation.util.MediaType
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.full_post.view.*
import ru.noties.markwon.Markwon

class FullPostView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var postDisplayType : MediaType? = null
    private val markwon = Markwon.create(context)
    private lateinit var post : PostModel

    private val defaultColor: Int
    private val downvotedColor: Int
    private val upvotedColor: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.full_post, this, true)
        defaultColor = context.resources.getColor(R.color.white)
        downvotedColor = context.resources.getColor(R.color.downvote)
        upvotedColor = context.resources.getColor(R.color.upvote)
    }

    fun setPost(postModel : PostModel) {
        post = postModel
        updateVoteView()
        updateSaveView()

        postDisplayType = MediaHelper.determineType(postModel)

        postModel.apply {
            postTitleView.text = title

            resources.getString(R.string.user_and_time, author, DateHelper.getDateDifferenceString(created!!)).apply {
                postAuthorView.text = resources.getString( R.string.post_age, this)
            }

            if (!selftext.isNullOrEmpty()) {
                val selfText = selftext!!
                markwon.setMarkdown(postSelfText, selfText)
                postSelfText.visibility = View.VISIBLE
            }

            postVoteCountView.text = score.toString()
            postCommentCountView.text = resources.getString(R.string.comment_count, commentCount)
        }

        fullPostTags.setPostTags(postModel)

        fullPostRootView.visibility = View.VISIBLE
        loadLinks(postModel)

        // display empty comment list message
        if (postModel.commentCount == 0) {
            postNoComments.visibility = View.VISIBLE
        } else {
            postNoComments.visibility = View.GONE
        }
    }

    fun setViewDelegate(delegate: Contract.PostAdapterDelegate) {
        postSaveView.setOnClickListener { delegate.interact(post, PostInteraction.Save) }
        postUpvoteView.setOnClickListener { delegate.interact(post, PostInteraction.Upvote) }
        postDownvoteView.setOnClickListener { delegate.interact(post, PostInteraction.Downvote) }
        postImageView.setOnClickListener { delegate.interact(post, PostInteraction.VisitLink) }
        postReplyView.setOnClickListener { delegate.interact(post, PostInteraction.NewReply) }
        postLinkCard.setOnClickListener { delegate.interact(post, PostInteraction.VisitLink) }
    }

    // region update view
    private fun updateVoteView() {
        when (post.userUpvoted) {
            1 -> {
                postUpvoteView.setColorFilter(upvotedColor)
                postDownvoteView.setColorFilter(defaultColor)
            }
            0 -> {
                postUpvoteView.setColorFilter(defaultColor)
                postDownvoteView.setColorFilter(defaultColor)
            }
            -1 -> {
                postUpvoteView.setColorFilter(defaultColor)
                postDownvoteView.setColorFilter(downvotedColor)
            }
        }
    }

    private fun updateSaveView() {
        // TODO convert all icons to use style colors
        val saveColor = if (post.saved) R.color.upvote else R.color.paleGray
        postSaveView.setColorFilter(resources.getColor(saveColor), PorterDuff.Mode.SRC_IN)
    }
    // endregion update view

    private fun loadLinks(postModel : PostModel) {
        when (postDisplayType) {
            MediaType.Image -> {
                displayPostProgress.visibility = View.VISIBLE
                Picasso.get()
                        .load(postModel.url).fit().centerCrop()
                        .into(postImageView, object : Callback {
                            override fun onSuccess() {
                                displayPostProgress.visibility = View.GONE
                            }

                            override fun onError(e: Exception?) {
                                // TODO handle
                            }
                        })

                postImageView.visibility = View.VISIBLE
            }
            MediaType.Gfycat -> {
                displayPostProgress.visibility  = View.VISIBLE
                Picasso.get()
                        .load(postModel.thumbnail).fit().centerCrop()
                        .into(postImageView, object : Callback {
                            override fun onSuccess() {
                                displayPostProgress.visibility = View.GONE
                            }

                            override fun onError(e: Exception?) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }
                        })
                postImageView.visibility = View.VISIBLE
            }
            else -> {
                postModel.thumbnail?.apply {
                    Picasso.get().load(postModel.thumbnail).fit().centerCrop().into(postLinkThumbnail)
                    postLinkUrl.text = postModel.url
                    postLinkCard.visibility = View.VISIBLE
                }
            }
        }
    }
}