package com.relic.presentation.displaypost

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.relic.R
import com.relic.data.models.PostModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.full_post.view.*
import java.util.Arrays

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
            postInfoView.text = author
            postSelfText.text = selftext

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

            postReplyView.setOnClickListener {
                // TODO
            }

        }

        loadLinks(postModel)
    }


    private fun loadLinks(postModel : PostModel) {
        if (displayImage) {
            Picasso.get().load(postModel.url).fit().centerCrop().into(postImageView)
        } else {
            postImageView.visibility = View.GONE

            if (!postModel.thumbnail.isNullOrEmpty())  {
                Picasso.get().load(postModel.thumbnail).fit().centerCrop().into(postLinkThumbnail)
                postLinkUrl.text = postModel.url
                postLinkCard.visibility = View.VISIBLE
            }
        }
    }
}