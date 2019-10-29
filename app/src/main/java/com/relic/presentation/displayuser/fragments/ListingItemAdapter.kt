package com.relic.presentation.displayuser.fragments

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.preference.PostViewPreferences
import com.relic.presentation.base.ComponentList
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.comments.CommentItemVH
import com.relic.presentation.displaypost.comments.RelicCommentView
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.list.PostItemVH
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

    // not the ideal solution but necessary for moving forward
    private val postComponentList = object : ComponentList<PostModel> {
        override fun getItem(position: Int): PostModel = listingItems[position] as PostModel
    }
    private val commentComponentList = object : ComponentList<CommentModel> {
        override fun getItem(position: Int): CommentModel = listingItems[position] as CommentModel
    }

    override fun getItemCount(): Int = listingItems.size

    override fun getItemViewType(position: Int): Int {
        return when (listingItems[position]) {
            is PostModel -> VIEW_TYPE_POST
            else -> VIEW_TYPE_COMMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POST ->  {
                val postItemView = RelicPostItemView(parent.context, postLayout = postLayout)
                PostItemVH(postItemView, postComponentList, postInteractor)
            }
            // TODO complete the custom comment VH and view for displaying within the tab
            else -> {
                val commentView = RelicCommentView(parent.context).apply {
                    displayParent(true)
                }
                CommentItemVH(commentView, commentComponentList, commentInteractor)
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

                return oldItem.fullName == newItem.fullName && oldItem.visited == newItem.visited
            }
        }).dispatchUpdatesTo(this)

        listingItems = newListingItems
    }

    fun clear() {
        listingItems = emptyList()
        notifyDataSetChanged()
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