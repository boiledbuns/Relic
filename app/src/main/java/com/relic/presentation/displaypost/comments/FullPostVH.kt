package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.presentation.base.ComponentList
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.FullPostView
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.list.PostItemAdapter

class FullPostVH(
    private val fullPostView : FullPostView,
    private val postList: ComponentList<PostModel>,
    private val postInteractor: DisplaySubContract.PostAdapterDelegate
) : RecyclerView.ViewHolder(fullPostView), DisplaySubContract.PostViewDelegate {

    fun bindPost(post : PostModel) {
        fullPostView.setPost(post)
    }

    // region PostViewDelegate

    override fun onPostPressed() = postInteractor.visitPost(getPost(adapterPosition))

    override fun onPostSavePressed() = postInteractor.savePost(getPost(adapterPosition))

    override fun onPostUpvotePressed() = postInteractor.voteOnPost(getPost(adapterPosition), UPVOTE_PRESSED)

    override fun onPostDownvotePressed() = postInteractor.voteOnPost(getPost(adapterPosition), DOWNVOTE_PRESSED)

    override fun onPostReply() = postInteractor.onNewReplyPressed( getPost(adapterPosition))

    override fun onPostLinkPressed() = postInteractor.onLinkPressed(getPost(adapterPosition))

    override fun onUserPressed() = postInteractor.previewUser(getPost(adapterPosition))

    // endregion PostViewDelegate

    private fun getPost(position : Int) = postList.getItem(position)
}