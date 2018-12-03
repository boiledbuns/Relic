package com.relic.presentation.displaysub.list

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItem
import com.relic.presentation.displaysub.DisplaySubContract

class PostItemAdapter (
        private val postAdapterDelegate : DisplaySubContract.PostAdapterDelegate
) : RecyclerView.Adapter <PostItemVH> () {
    private var postList: MutableList<PostModel> = ArrayList()

    override fun getItemCount() = postList.size

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PostItemVH {
        return PostItemVH(RelicPostItem(parent.context)).apply {
            initializeOnClicks(this@PostItemAdapter)
        }
    }

    override fun onBindViewHolder(viewholder: PostItemVH, position: Int) {
        viewholder.bindPost(postList[position], position)
    }

    fun clear() {
        postList = ArrayList()
        notifyDataSetChanged()
    }

    fun setPostList(newPostList: List<PostModel>) {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return postList.size
            }

            override fun getNewListSize(): Int {
                return newPostList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return postList[oldItemPosition].id == newPostList[newItemPosition].id
            }

            override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
            ): Boolean {
                val oldPost = postList[oldItemPosition]
                val newPost = newPostList[newItemPosition]

                return oldPost.id == newPost.id && oldPost.isVisited == newPost.isVisited
            }
        }).dispatchUpdatesTo(this)

        postList.clear()
        postList.addAll(newPostList)
    }

    // start region for onclick handlers

    fun onPostPressed (itemPosition : Int) {
        postList[itemPosition].also {
            // update the view and local model to reflect onclick
            it.isVisited = true

            // update post to show that it has been visited
            postAdapterDelegate.visitPost(it.id, it.subreddit)
        }
        notifyItemChanged(itemPosition)
    }

    // initialize onclick for the upvote button
    fun onPostUpvotePressed(itemPosition : Int) {
        postList[itemPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newStatus = if (it.userUpvoted <= 0) 1 else 0

            // optimistic, update copy cached in adapter and make request to api to update in server
            it.userUpvoted = newStatus
            postAdapterDelegate.voteOnPost(it.id, newStatus)
        }
        notifyItemChanged(itemPosition)
    }

    // initialize onclick for the downvote button
    fun onPostDownvotePressed(itemPosition : Int) {
        postList[itemPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newStatus = if (it.userUpvoted >= 0) -1 else 0

            // optimistic, update copy cached in adapter and make request to api to update in server
            it.userUpvoted = newStatus
            postAdapterDelegate.voteOnPost(it.id, newStatus)
        }
        notifyItemChanged(itemPosition)
    }

    fun onPostSavePressed (itemPosition : Int) {
        postList[itemPosition].also {
            // calculate new save value based on the previous one and tell vm to update appropriately
            val newStatus = !it.isSaved
            postAdapterDelegate.savePost(it.id, newStatus)

            // update the view and local model to reflect onclick
            it.isSaved = newStatus
        }
        notifyItemChanged(itemPosition)
    }

    fun onPostLinkPressed (itemPosition : Int) {
        postAdapterDelegate.onThumbnailClicked(postList[itemPosition].url)
    }
    // end region for onclick handlers
}