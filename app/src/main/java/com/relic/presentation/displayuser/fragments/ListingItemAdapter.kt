package com.relic.presentation.displayuser.fragments

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.interactor.Contract
import com.relic.preference.PostViewPreferences
import com.relic.presentation.base.ItemNotifier
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaypost.comments.RelicPostCommentView
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.post_comment_item.view.*

private const val VIEW_TYPE_POST = 0
private const val VIEW_TYPE_COMMENT = 1

class ListingItemAdapter(
    private val viewPrefsManager: PostViewPreferences,
    private val postInteractor : Contract.PostAdapterDelegate,
    private val commentInteractor: Contract.CommentAdapterDelegate
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
                val postCommentView = RelicPostCommentView(parent.context)
                PostCommentItemVH(postCommentView)
            }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position : Int) {
        when (val item = listingItems[position]) {
            is PostModel -> (vh as PostItemVH).bind(item)
            is CommentModel -> (vh as PostCommentItemVH).bind(item)
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

    private inner class PostCommentItemVH (
      private val postCommentView : RelicPostCommentView
    ): RecyclerView.ViewHolder(postCommentView), ItemNotifier{

        init { postCommentView.post_comment.setViewDelegate(commentInteractor, this) }

        fun bind(commentModel : CommentModel) {
            postCommentView.setComment(commentModel)
            postCommentView.setViewDelegate(commentInteractor, this)
            postCommentView.post_comment.setComment(commentModel)
        }

        override fun notifyItem() {
            notifyItemChanged(layoutPosition)
        }
    }

    private inner class PostItemVH(
      private val postItemView : RelicPostItemView
    ) : RecyclerView.ViewHolder(postItemView), ItemNotifier {

        init { postItemView.setViewDelegate(postInteractor, this) }

        fun bind(postModel: PostModel) {
            postItemView.setPost(postModel)
        }

        override fun notifyItem() {
            notifyItemChanged(layoutPosition)
        }
    }

    // TODO consider refactoring onclicks for both posts and comments to consolidate them into a single interface
}