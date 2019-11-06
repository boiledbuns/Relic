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
import com.relic.presentation.displaysub.PostInteraction
import com.relic.presentation.displaysub.PostViewDelegate
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
    ) : RecyclerView.ViewHolder(postItemView) {

        private val delegate = object : PostViewDelegate(postInteractor) {
            override fun getPost() = listingItems[layoutPosition] as PostModel
        }

        init { postItemView.setViewDelegate(delegate) }

        fun bind(postModel: PostModel) {
            postItemView.setPost(postModel)
        }
    }

    // TODO consider refactoring onclicks for both posts and comments to consolidate them into a single interface
}