package com.relic.presentation.displaysub.list

import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.presentation.base.ComponentList
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.android.synthetic.main.post_item_content.view.*

class PostItemVH (
    private val postItemView : RelicPostItemView,
    private val postList: ComponentList<PostModel>,
    private val postInteractor: DisplaySubContract.PostAdapterDelegate
) : RecyclerView.ViewHolder(postItemView), DisplaySubContract.PostViewDelegate {

    init {
        itemView.apply {
            setOnClickListener { onPostPressed() }
            postItemSaveView.setOnClickListener { onPostSavePressed() }
            postItemUpvoteView.setOnClickListener { onPostUpvotePressed() }
            postItemDownvoteView.setOnClickListener { onPostDownvotePressed() }
            postItemThumbnailView.setOnClickListener { onPostLinkPressed() }
            postItemCommentView.setOnClickListener { onPostReply() }
            postItemAuthorView.setOnClickListener { onUserPressed() }
        }
    }

    fun bindPost(postModel : PostModel) {
        postItemView.setPost(postModel)
    }

    // region post view delegate

    override fun onPostPressed() = postInteractor.visitPost(getPost(adapterPosition))

    override fun onPostSavePressed() = postInteractor.savePost(getPost(adapterPosition))

    override fun onPostUpvotePressed() = postInteractor.voteOnPost(getPost(adapterPosition), UPVOTE_PRESSED)

    override fun onPostDownvotePressed() = postInteractor.voteOnPost(getPost(adapterPosition), DOWNVOTE_PRESSED)

    override fun onPostReply() = postInteractor.onNewReplyPressed(getPost(adapterPosition))

    override fun onPostLinkPressed() = postInteractor.onLinkPressed(getPost(adapterPosition))

    override fun onUserPressed() = postInteractor.previewUser(getPost(adapterPosition))

    // endregion post view delegate

    private fun getPost(position : Int) : PostModel = postList.getItem(position)
}