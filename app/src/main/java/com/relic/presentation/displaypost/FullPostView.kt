package com.relic.presentation.displaypost

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.relic.R
import com.relic.data.models.PostModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.full_post.view.*

class FullPostView @JvmOverloads constructor(
        context : Context,
        attrs: AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val TAG = "FULL_POST_VIEW"
    private lateinit var viewDelegate : DisplayPostContract.PostViewDelegate
    private var displayImage : Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.full_post, this, true)
    }

    fun setPost(postModel : PostModel, isImage : Boolean, delegate : DisplayPostContract.PostViewDelegate) {
        viewDelegate = delegate
        displayImage = isImage

        postModel.apply {
            postTitleView.text = title
            postAuthorView.text = resources.getString(R.string.user_and_time, author, created)

            if (!linkFlair.isNullOrEmpty()) {
                postTagView.text = linkFlair
                postTagView.visibility = View.VISIBLE
            }

            if (!authorFlair.isNullOrEmpty()) {
                postAuthorFlairView.text = authorFlair
                postAuthorFlairView.visibility = View.VISIBLE
            }

            if (!selftext.isNullOrEmpty()) {
                postSelfText.text = selftext
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

        initializeOnClicks(delegate)
        loadLinks(postModel)
    }

    private fun initializeOnClicks(viewDelegate : DisplayPostContract.PostViewDelegate) {
        postImageView.setOnClickListener { viewDelegate.onImagePressed() }
        postUpvoteView.setOnClickListener { viewDelegate.onPostVoted(1) }
        postDownvoteView.setOnClickListener { viewDelegate.onPostVoted(-1) }
    }

    private fun loadLinks(postModel : PostModel) {
        when {
            displayImage -> {
                Picasso.get().load(postModel.url).fit().centerCrop().into(postImageView)
                postLinkCard.visibility = View.GONE
            }
            !(postModel.thumbnail.isNullOrEmpty()) -> {
                Picasso.get().load(postModel.thumbnail).fit().centerCrop().into(postLinkThumbnail)
                postLinkUrl.text = postModel.url
                postImageView.visibility = View.GONE
            }
            else -> {
                // no link, so just hide both the image and thumbnail
                postImageView.visibility = View.GONE
                postLinkCard.visibility = View.GONE
            }
        }
    }
}