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
import ru.noties.markwon.Markwon

class FullPostView @JvmOverloads constructor(
        context : Context,
        attrs: AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val TAG = "FULL_POST_VIEW"
    private lateinit var viewDelegate : DisplayPostContract.PostViewDelegate
    private var postDisplayType : DisplayPostType? = null
    private val markwon = Markwon.create(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.full_post, this, true)
    }

    fun setPost(postModel : PostModel, displayType : DisplayPostType?, delegate : DisplayPostContract.PostViewDelegate) {
        viewDelegate = delegate
        postDisplayType = displayType

        postModel.apply {
            postTitleView.text = title
            postAuthorView.text = resources.getString(R.string.user_and_time, author, created)

            if (isNsfw) postNSFWView.visibility = View.VISIBLE

            postTagView.text = linkFlair
            if (!linkFlair.isNullOrEmpty()) postTagView.visibility = View.VISIBLE

            postAuthorFlairView.text = authorFlair
            if (!authorFlair.isNullOrEmpty()) postAuthorFlairView.visibility = View.VISIBLE

            if (!selftext.isNullOrEmpty()) {
                markwon.setMarkdown(postSelfText, selftext)
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

        fullPostRootView.visibility = View.VISIBLE
        initializeOnClicks(delegate)
        loadLinks(postModel)
    }

    private fun initializeOnClicks(viewDelegate : DisplayPostContract.PostViewDelegate) {
        postImageView.setOnClickListener { viewDelegate.onLinkPressed() }
        postUpvoteView.setOnClickListener { viewDelegate.onPostVoted(1) }
        postDownvoteView.setOnClickListener { viewDelegate.onPostVoted(-1) }
        postReplyView.setOnClickListener { viewDelegate.onReplyPressed() }
        postLinkCard.setOnClickListener { viewDelegate.onLinkPressed() }
    }

    private fun loadLinks(postModel : PostModel) {
        when (postDisplayType) {
            DisplayPostType.Image -> {
                Picasso.get().load(postModel.url).fit().centerCrop().into(postImageView)
                postImageView.visibility = View.VISIBLE
            }
            DisplayPostType.Link -> {
                Picasso.get().load(postModel.thumbnail).fit().centerCrop().into(postLinkThumbnail)
                postLinkUrl.text = postModel.url
                postLinkCard.visibility = View.VISIBLE
            }
        }
    }
}