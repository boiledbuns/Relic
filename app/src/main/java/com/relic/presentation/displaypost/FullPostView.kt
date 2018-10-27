package com.relic.presentation.displaypost

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
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
    private val picEndings = Arrays.asList("jpg", "png")

    private val TAG = "FULL_POST_VIEW"

    init {
        LayoutInflater.from(context).inflate(R.layout.full_post, this, true)
    }

    fun setPost(postModel : PostModel, delegate : DisplayPostContract.PostViewDelegate) {
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
        // loadLinkPreview(url)
    }


    // TODO move concern into vm
    private fun loadLinkPreview(postModel: PostModel) {
        val linkUrl = postModel.domain
        val notEmpty = !linkUrl.isEmpty()
        var isImage = false
        val validUrls = Arrays.asList("self", "i.re")
        Log.d(TAG, linkUrl.substring(0, 4))

        if (notEmpty && !validUrls.contains(linkUrl.substring(0, 4))) {
            // loads the card image
            Picasso.get().load(postModel.thumbnail).fit().centerCrop()
                    .into(display_post_card_thumbnail)
        } else {
            val fullUrl = postModel.url
            // load the full image for the image
            if (picEndings.contains(fullUrl.substring(fullUrl.length - 3))) {
                try {
                    Picasso.get().load(fullUrl).fit().centerCrop().into(postImageView)
                    isImage = true
                } catch (error: Error) {
                    Log.d("DISPLAYPOST_VIEW", "Issue loading image " + error.toString())
                }
            }
        }
    }


}