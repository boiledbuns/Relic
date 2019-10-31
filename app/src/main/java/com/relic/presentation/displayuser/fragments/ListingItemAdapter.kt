package com.relic.presentation.displayuser.fragments

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.preference.PostViewPreferences
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.displaypost.comments.RelicCommentView
import com.relic.presentation.displaysub.DisplaySubContract
import ru.noties.markwon.Markwon

private const val VIEW_TYPE_POST = 0
private const val VIEW_TYPE_COMMENT = 1

class ListingItemAdapter(
    private val viewPrefsManager: PostViewPreferences,
    private val postInteractor : DisplaySubContract.PostAdapterDelegate,
    private val commentInteractor: DisplayPostContract.CommentAdapterDelegate
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var markwon : Markwon

    private val postLayout = viewPrefsManager.getPostCardStyle()

    private var listingItems : List<ListingItem> = ArrayList()

    override fun getItemCount(): Int = listingItems.size

    override fun getItemViewType(position: Int): Int {
        return when (listingItems[position]) {
            is PostModel -> VIEW_TYPE_POST
            else -> VIEW_TYPE_COMMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POST ->  PostItemVH(RelicPostItemView(parent.context, postLayout = postLayout))
            // TODO complete the custom comment VH and view for displaying within the tab
            else -> {
                val commentView = RelicCommentView(parent.context).apply {
                    displayParent(true)
                }
                CommentItemVH(commentView)
            }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position : Int) {
        when (val item = listingItems[position]) {
            is PostModel -> (vh as PostItemVH).bind(item)
            is CommentModel -> (vh as CommentItemVH).bind(item)
        }
    }

    fun setItems(newListingItems : List<ListingItem>) {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return listingItems.size
            }

            override fun getNewListSize(): Int {
                return newListingItems.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listingItems[oldItemPosition].fullName == newListingItems[newItemPosition].fullName
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                val oldItem = listingItems[oldPosition]
                val newItem = newListingItems[newPosition]

                return oldItem.fullName == newItem.fullName && oldItem.visited == newItem.visited
            }
        }).dispatchUpdatesTo(this)

        listingItems = newListingItems
    }

    fun clear() {
        listingItems = emptyList()
        notifyDataSetChanged()
    }

    private inner class CommentItemVH (
      private val commentView : RelicCommentView
    ): RecyclerView.ViewHolder(commentView), DisplayPostContract.CommentViewDelegate{

        init { commentView.setViewDelegate(this) }

        fun bind(commentModel : CommentModel) {
            commentView.setComment(commentModel)
        }

        override fun voteOnComment(voteValue: Int) = commentInteractor.onCommentVoted(getComment(layoutPosition), voteValue)
        override fun replyToComment(text: String) = commentInteractor.onReplyPressed(getComment(layoutPosition), text)
        override fun previewUser() = commentInteractor.onPreviewUser(getComment(layoutPosition))
        override fun loadMoreComments(displayReplies: Boolean) = commentInteractor.onExpandReplies(getComment(layoutPosition))
        override fun visitComment() {}

        private fun getComment(position: Int) = listingItems[position] as CommentModel
    }

    private inner class PostItemVH(
      private val postItemView : RelicPostItemView
    ) : RecyclerView.ViewHolder(postItemView), DisplaySubContract.PostViewDelegate {

        init { postItemView.setViewDelegate(this) }

        fun bind(postModel: PostModel) {
            postItemView.setPost(postModel)
        }

        override fun onPostPressed() = postInteractor.visitPost(getPost(layoutPosition))
        override fun onPostSavePressed() = postInteractor.savePost(getPost(layoutPosition), !getPost(layoutPosition).saved)
        override fun onPostUpvotePressed() = postInteractor.voteOnPost(getPost(layoutPosition), UPVOTE_PRESSED)
        override fun onPostDownvotePressed() = postInteractor.voteOnPost(getPost(layoutPosition), DOWNVOTE_PRESSED)
        override fun onPostReply() = postInteractor.onNewReplyPressed(getPost(layoutPosition))
        override fun onPostLinkPressed() = postInteractor.onLinkPressed(getPost(layoutPosition))
        override fun onUserPressed() = postInteractor.previewUser(getPost(layoutPosition))

        private fun getPost(position: Int) = listingItems[position] as PostModel
    }


    // start region for onclick handlers

//    override fun onPostPressed (itemPosition : Int) {
//        listingItems[itemPosition].also {
//            // update post to show that it has been visited
//            actionDelegate.visitListing(it)
//
//            // update the view and local model to reflect onclick
//            it.visited = true
//        }
//        notifyItemChanged(itemPosition)
//    }
//
//    override fun onPostUpvotePressed(itemPosition : Int, notify : Boolean) {
//        listingItems[itemPosition].also {
//            // determine the new vote value based on the current one and change the vote accordingly
//            val newVote = if (it.userUpvoted <= 0) 1 else 0
//            it.userUpvoted = newVote
//            notifyItemChanged(itemPosition)
//
//            actionDelegate.voteOnListing(it, newVote)
//        }
//    }
//
//    override fun onPostDownvotePressed(itemPosition : Int, notify : Boolean) {
//        listingItems[itemPosition].also {
//            // determine the new vote value based on the current one and change the vote accordingly
//            val newVote = if (it.userUpvoted >= 0) -1 else 0
//
//            // optimistic, update copy cached in adapter and make request to api to update in server
//            it.userUpvoted = newVote
//            notifyItemChanged(itemPosition)
//
//            actionDelegate.voteOnListing(it, newVote)
//        }
//    }
//
//    override fun onPostSavePressed (itemPosition : Int) {
//        listingItems[itemPosition].also {
//            // update the view and local model to reflect onclick
//            it.saved = !it.saved
//            notifyItemChanged(itemPosition)
//
//            actionDelegate.saveListing(it)
//        }
//    }
//
//    override fun onPostLinkPressed (itemPosition : Int) {
//        actionDelegate.onThumbnailClicked(listingItems[itemPosition])
//    }
//
//    override fun onUserPressed(itemPosition: Int) {
//        actionDelegate.onUserClicked(listingItems[itemPosition])
//    }
//
//    override fun loadMoreComments(itemPosition: Int, displayReplies: Boolean) { }

    // end region for onclick handlers

    // TODO consider refactoring onclicks for both posts and comments to consolidate them into a single interface
}