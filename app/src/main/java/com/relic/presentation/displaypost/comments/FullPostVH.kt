package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.FullPostView
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.list.PostItemAdapter

class FullPostVH(
    private val fullPostView : FullPostView,
    private val postAdapter: PostItemAdapter,
    private val postInteractor: DisplaySubContract.PostAdapterDelegate
) : RecyclerView.ViewHolder(fullPostView), DisplaySubContract.PostViewDelegate {

    fun bindPost(post : PostModel) {
        fullPostView.setPost(post)
    }

    // region PostViewDelegate

    override fun onPostPressed() = postInteractor.visitPost(postAdapter.post(adapterPosition))

    override fun onPostSavePressed() = postInteractor.savePost(postAdapter.post(adapterPosition))

    override fun onPostUpvotePressed() = postInteractor.voteOnPost(postAdapter.post(adapterPosition), UPVOTE_PRESSED)

    override fun onPostDownvotePressed() = postInteractor.voteOnPost(postAdapter.post(adapterPosition), DOWNVOTE_PRESSED)

    override fun onPostReply() = postInteractor.onNewReplyPressed( postAdapter.post(adapterPosition))

    override fun onPostLinkPressed() = postInteractor.onLinkPressed(postAdapter.post(adapterPosition))

    override fun onUserPressed() = postInteractor.previewUser(postAdapter.post(adapterPosition))

    // endregion PostViewDelegate

    private fun PostItemAdapter.post(position : Int) = getPosts().get(position)
}