package com.relic.presentation.displaysub.list

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.preference.PostViewPreferences
import com.relic.presentation.base.ItemNotifier
import com.relic.presentation.base.RelicAdapter
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.coroutines.launch
import ru.noties.markwon.Markwon

class PostItemAdapter (
    private val viewPrefsManager: PostViewPreferences,
    private val postInteractor : DisplaySubContract.PostAdapterDelegate
) : RelicAdapter<PostItemAdapter.PostItemVH>() {

    private var postList: List<PostModel> = ArrayList()
    private lateinit var markwon: Markwon
    private val postLayout = viewPrefsManager.getPostCardStyle()

    override fun getItemCount() = postList.size

    override fun getItemId(position: Int): Long = postList[position].id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PostItemVH {
        markwon = Markwon.create(parent.context)
        return PostItemVH(RelicPostItemView(parent.context, postLayout = postLayout))
    }

    override fun onBindViewHolder(viewholder: PostItemVH, position: Int) {
        viewholder.bindPost(postList[position])
    }

    fun clear() {
        setPostList(emptyList())
    }

    fun getPostList() : List<PostModel> = postList
    fun setPostList(newPostList: List<PostModel>) {
        launch {
            calculateDiff(newPostList).dispatchUpdatesTo(this@PostItemAdapter)
            postList = newPostList
        }
    }

    inner class PostItemVH (
      private val postItemView : RelicPostItemView
    ) : RecyclerView.ViewHolder(postItemView), ItemNotifier {

        override fun notifyItem() {
            notifyItemChanged(layoutPosition)
        }

        init { postItemView.setViewDelegate(postInteractor, this) }

        fun bindPost(postModel : PostModel) = postItemView.setPost(postModel)
    }

    private fun calculateDiff(newPostList: List<PostModel>): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = postList.size
            override fun getNewListSize() = newPostList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return postList[oldItemPosition].fullName == newPostList[newItemPosition].fullName
            }

            override fun areContentsTheSame(
              oldItemPosition: Int,
              newItemPosition: Int
            ): Boolean {
                val oldPost = postList[oldItemPosition]
                val newPost = newPostList[newItemPosition]

                return oldPost.visited == newPost.visited
            }
        })
    }
}

