package com.relic.presentation.displayuser.fragments

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.relic.data.models.CommentModel
import com.relic.data.models.ListingItem
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaypost.commentlist.CommentItemVH
import com.relic.presentation.displaypost.commentlist.RelicCommentView
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.list.PostItemVH

class ListingItemAdapter(
    private val actionDelegate : DisplaySubContract.PostAdapterDelegate
) : RecyclerView.Adapter <RecyclerView.ViewHolder> () {

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
            }
            // TODO complete the custom comment VH and view for displaying within the tab
            else -> CommentItemVH(RelicCommentView(parent.context)).apply {
            }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position : Int) {
        val item = listingItems[position]
        when (item) {
            is PostModel -> (vh as PostItemVH).bindPost(item, position)
            is CommentModel -> (vh as CommentItemVH).bindComment(item, position)
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
                return listingItems[oldItemPosition].id == newListingItems[newItemPosition].id
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                val oldItem = listingItems[oldPosition]
                val newItem = newListingItems[newPosition]

                return oldItem.id == newItem.id && oldItem.isVisited == newItem.isVisited
            }
        }).dispatchUpdatesTo(this)

        listingItems = newListingItems
    }

    fun clear() {
        listingItems = emptyList()
        notifyDataSetChanged()
    }

}