package com.relic.presentation.displaysub.list

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.coroutines.*
import ru.noties.markwon.Markwon

class PostItemAdapter (
        private val postAdapterDelegate : DisplaySubContract.PostAdapterDelegate
) : RecyclerView.Adapter <PostItemVH> (), DisplaySubContract.PostItemAdapterDelegate {

    private var postList: List<PostModel> = ArrayList()
    private lateinit var markwon : Markwon

    override fun getItemCount() = postList.size

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PostItemVH {
        markwon = Markwon.create(parent.context)
        val postItemView = RelicPostItemView(parent.context)

        return PostItemVH(postItemView).apply {
            initializeOnClicks(this@PostItemAdapter)
        }
    }

    override fun onBindViewHolder(viewholder: PostItemVH, position: Int) {
        viewholder.bindPost(postList[position])
    }

    fun clear() { setPostList(emptyList()) }

    fun setPostList(newPostList: List<PostModel>) {
//        GlobalScope.launch {
//            val diff = calculateDiff(newPostList)
//
//            GlobalScope.launch(Dispatchers.Main) {
//                diff.dispatchUpdatesTo(this@PostItemAdapter)
//                postList = newPostList
//                Log.d("post_item_adapter", "post item test")
//            }
//        }
        calculateDiff(newPostList).dispatchUpdatesTo(this)
        postList = newPostList
    }

    // region onclick handlers

    override fun onPostPressed (itemPosition : Int) {
        postList[itemPosition].also {
            // update post to show that it has been visited
            postAdapterDelegate.visitPost(it.fullName, it.subreddit!!)
            // update the view and local model to reflect onclick
            it.visited = true
        }
        notifyItemChanged(itemPosition)
    }

    // initialize onclick for the upvote button
    override fun onPostUpvotePressed(itemPosition : Int, notify : Boolean) {
        postList[itemPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newStatus = if (it.userUpvoted <= 0) 1 else 0

            // optimistic, update copy cached in adapter and make request to api to update in server
            it.userUpvoted = newStatus
            postAdapterDelegate.voteOnPost(it.fullName, newStatus)
        }
        if (notify) notifyItemChanged(itemPosition)
    }

    // initialize onclick for the downvote button
    override fun onPostDownvotePressed(itemPosition : Int, notify : Boolean) {
        postList[itemPosition].also {
            // determine the new vote value based on the current one and change the vote accordingly
            val newStatus = if (it.userUpvoted >= 0) -1 else 0

            // optimistic, update copy cached in adapter and make request to api to update in server
            it.userUpvoted = newStatus
            postAdapterDelegate.voteOnPost(it.fullName, newStatus)
        }
        if (notify) notifyItemChanged(itemPosition)
    }

    override fun onPostSavePressed (itemPosition : Int) {
        postList[itemPosition].also {
            // calculate new save value based on the previous one and tell vm to update appropriately
            val newStatus = !it.saved
            postAdapterDelegate.savePost(it.fullName, newStatus)

            // update the view and local model to reflect onclick
            it.saved = newStatus
        }
        notifyItemChanged(itemPosition)
    }

    override fun onPostLinkPressed (itemPosition : Int) {
        postAdapterDelegate.onLinkPressed(postList[itemPosition].url!!)
    }

    override fun onUserPressed(itemPosition: Int) {
        postAdapterDelegate.previewUser(postList[itemPosition].author)
    }

    // end region for onclick handlers

    private fun calculateDiff (newPostList: List<PostModel>) : DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return postList.size
            }

            override fun getNewListSize(): Int {
                return newPostList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return postList[oldItemPosition].fullName == newPostList[newItemPosition].fullName
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                val oldPost = postList[oldItemPosition]
                val newPost = newPostList[newItemPosition]

                return oldPost.fullName == newPost.fullName && oldPost.visited == newPost.visited
            }
        })
    }
}

