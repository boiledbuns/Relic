package com.relic.presentation.displaysub.list

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.relic.data.models.PostModel
import com.relic.presentation.customview.RelicPostItem
import com.relic.presentation.displaysub.DisplaySubContract

class PostItemAdapter (
        private val subViewDelegate : DisplaySubContract.SubViewDelegate

) : RecyclerView.Adapter <PostItemVH> () {
    private var postList: MutableList<PostModel> = ArrayList()

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PostItemVH {
        return PostItemVH(RelicPostItem(parent.context)).apply {
            bindPost(postList[position])
        }
    }

    override fun onBindViewHolder(viewholder: PostItemVH, position: Int) {
        viewholder.bindPost(postList[position])
    }

    fun clear() {
        postList = ArrayList()
    }

    fun setPostList(newPostList: MutableList<PostModel>) {
        if (postList.size == 0) {
            postList = newPostList
            notifyItemRangeRemoved(0, newPostList.size)
        } else {
            // used to tell list what has changed
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
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
            })
            // sets the new list as the current one
            postList = newPostList
            diffResult.dispatchUpdatesTo(this)
        }
    }
}