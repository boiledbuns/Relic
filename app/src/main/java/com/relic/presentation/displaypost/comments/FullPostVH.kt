package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.FullPostView

class FullPostVH(
    private val fullPost : FullPostView
) : androidx.recyclerview.widget.RecyclerView.ViewHolder(fullPost) {

    fun bindPost(post : PostModel) {
        fullPost.setPost(post)
    }

    fun initializeOnClicks(delegate : DisplayPostContract.PostViewDelegate) {
        fullPost.setOnClicks(delegate)
    }

}