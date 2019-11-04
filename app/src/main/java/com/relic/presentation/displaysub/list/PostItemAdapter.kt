package com.relic.presentation.displaysub.list

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.preference.PostViewPreferences
import com.relic.presentation.base.ComponentList
import com.relic.presentation.base.RelicAdapter
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.coroutines.launch
import ru.noties.markwon.Markwon

class PostItemAdapter (
    private val viewPrefsManager: PostViewPreferences,
    private val postInteractor : DisplaySubContract.PostAdapterDelegate
) : RelicAdapter<PostItemAdapter.PostItemVH>(), ComponentList<PostModel> {

    private var postList: List<PostModel> = ArrayList()
    private lateinit var markwon: Markwon
    private val postLayout = viewPrefsManager.getPostCardStyle()

    override fun getItem(position: Int) = postList.get(position)

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

    fun setPostList(newPostList: List<PostModel>) {
        launch {
            calculateDiff(newPostList).dispatchUpdatesTo(this@PostItemAdapter)
            postList = newPostList
        }
    }

    inner class PostItemVH (
      private val postItemView : RelicPostItemView
    ) : RecyclerView.ViewHolder(postItemView), DisplaySubContract.PostViewDelegate {

        init { postItemView.setViewDelegate(this) }

        fun bindPost(postModel : PostModel) = postItemView.setPost(postModel)

        // region post view delegate

        override fun onPostPressed() = postInteractor.visitPost(getPost())
        override fun onPostSavePressed() = postInteractor.savePost(getPost(), getPost().saved)
        override fun onPostUpvotePressed() = postInteractor.voteOnPost(getPost(), UPVOTE_PRESSED)
        override fun onPostDownvotePressed() = postInteractor.voteOnPost(getPost(), DOWNVOTE_PRESSED)
        override fun onPostReply() = postInteractor.onNewReplyPressed(getPost())
        override fun onPostLinkPressed() = postInteractor.onLinkPressed(getPost())
        override fun onUserPressed() = postInteractor.previewUser(getPost())

        // endregion post view delegate

        private fun getPost() : PostModel = postList[layoutPosition]
    }

    private suspend fun calculateDiff(newPostList: List<PostModel>): DiffUtil.DiffResult {
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

                return oldPost.visited == newPost.visited
            }
        })
    }
}

