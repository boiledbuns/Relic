package com.relic.presentation.displaypost

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.presentation.helper.DateHelper
import com.relic.presentation.util.MediaType
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.full_post.view.*
import ru.noties.markwon.Markwon

class FullPostView @JvmOverloads constructor(
        context : Context,
        attrs: AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val TAG = "FULL_POST_VIEW"
    private lateinit var viewDelegate : DisplayPostContract.PostViewDelegate
    private var postDisplayType : MediaType? = null
    private val markwon = Markwon.create(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.full_post, this, true)
    }

    fun setPost(postModel : PostModel, displayType : MediaType?, delegate : DisplayPostContract.PostViewDelegate) {
        viewDelegate = delegate
        postDisplayType = displayType

        postModel.apply {
            postTitleView.text = title
            postAuthorView.text = resources.getString(R.string.user_and_time, author, DateHelper.getDateDifferenceString(created!!))

            if (!selftext.isNullOrEmpty()) {
                val selfText = selftext!!
                markwon.setMarkdown(postSelfText, selfText)
                postSelfText.visibility = View.VISIBLE
            }

            when (userUpvoted) {
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

            postVoteCountView.text = score.toString()
            postCommentCountView.text = commentCount.toString()
        }

        fullPostTags.setPostTags(postModel)

        fullPostRootView.visibility = View.VISIBLE
        initializeOnClicks(delegate)
        loadLinks(postModel)
    }

    private fun initializeOnClicks(viewDelegate : DisplayPostContract.PostViewDelegate) {
        postImageView.setOnClickListener { viewDelegate.onLinkPressed() }
        postUpvoteView.setOnClickListener { viewDelegate.onPostVoted(1) }
        postDownvoteView.setOnClickListener { viewDelegate.onPostVoted(-1) }
        postReplyView.setOnClickListener { viewDelegate.onNewReplyPressed() }
        postLinkCard.setOnClickListener { viewDelegate.onLinkPressed() }
    }

    private fun loadLinks(postModel : PostModel) {
        when (postDisplayType) {
            MediaType.Image -> {
                Picasso.get().load(postModel.url).fit().centerCrop().into(postImageView)
                postImageView.visibility = View.VISIBLE
            }
            MediaType.Gfycat -> {
                Picasso.get().load(postModel.thumbnail).fit().centerCrop().into(postImageView)
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