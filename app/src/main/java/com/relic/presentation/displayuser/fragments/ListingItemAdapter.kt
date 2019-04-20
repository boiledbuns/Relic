package com.relic.presentation.displayuser.fragments

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.relic.data.models.CommentModel
import com.relic.data.models.ListingItem
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.commentlist.CommentItemVH
import com.relic.presentation.displaypost.commentlist.RelicCommentView
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.list.PostItemVH
import com.relic.presentation.displayuser.DisplayUserContract

class ListingItemAdapter(
    private val actionDelegate : DisplayUserContract.ListingItemAdapterDelegate
) : RecyclerView.Adapter <RecyclerView.ViewHolder> (),
    DisplaySubContract.PostItemAdapterDelegate, DisplayPostContract.CommentAdapterDelegate {

    private val VIEW_TYPE_POST = 0
    private val VIEW_TYPE_COMMENT = 1

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
            VIEW_TYPE_POST -> PostItemVH(RelicPostItemView(parent.context)).apply {
                initializeOnClicks(this@ListingItemAdapter)
            }
            // TODO complete the custom comment VH and view for displaying within the tab
            else -> {
                val commentView = RelicCommentView(parent.context)
                commentView.displayParent(true)
                CommentItemVH(commentView).apply {
                    initializeOnClicks(this@ListingItemAdapter)
                }
            }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position : Int) {
        val item = listingItems[position]
        when (item) {
            is PostModel -> (vh as PostItemVH).bindPost(item)
            is CommentModel -> (vh as CommentItemVH).bindComment(item)
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

                return oldItem.fullName == newItem.fullName && oldItem.isVisited == newItem.isVisited
            }
        }).dispatchUpdatesTo(this)

        listingItems = newListingItems
    }

    fun clear() {
        listingItems = emptyList()
        notifyDataSetChanged()
    }


    // start region for onclick handlers

    override fun onPostPressed (itemPosition : Int) {
        listingItems[itemPosition].also {
            // update post to show that it has been visited
            actionDelegate.visitListing(it)

            // update the view and local model to reflect onclick
            it.isVisited = true
        }
        notifyItemChanged(itemPosition)
    }

    override fun onPostUpvotePressed(itemPosition : Int) {
        listingItems[itemPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newVote = if (it.userUpvoted <= 0) 1 else 0
            it.userUpvoted = newVote
            notifyItemChanged(itemPosition)

            actionDelegate.voteOnListing(it, newVote)
        }
    }

    override fun onPostDownvotePressed(itemPosition : Int) {
        listingItems[itemPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newVote = if (it.userUpvoted >= 0) -1 else 0

            // optimistic, update copy cached in adapter and make request to api to update in server
            it.userUpvoted = newVote
            notifyItemChanged(itemPosition)

            actionDelegate.voteOnListing(it, newVote)
        }
    }

    override fun onPostSavePressed (itemPosition : Int) {
        listingItems[itemPosition].also {
            // update the view and local model to reflect onclick
            it.saved = !it.saved
            notifyItemChanged(itemPosition)

            actionDelegate.saveListing(it)
        }
    }

    override fun onPostLinkPressed (itemPosition : Int) {
        actionDelegate.onThumbnailClicked(listingItems[itemPosition])
    }

    // end region for onclick handlers

    // TODO consider refactoring onclicks for both posts and comments to consolidate them into a single interface
    // region comment adapter delegate

    override fun displayCommentReplies(itemId: String, commentExpanded: Boolean) {}

    override fun voteOnComment(itemPosition: Int, voteValue: Int) {
        if (voteValue == 1) {
            onPostUpvotePressed(itemPosition)
        } else if (voteValue == -1){
            onPostDownvotePressed(itemPosition)
        }
    }

    override fun replyToComment(itemPosition: Int) {}

    override fun visitComment(itemPosition: Int) {
         actionDelegate.visitListing(listingItems[itemPosition])
    }

    // endregion comment adapter delegate
}