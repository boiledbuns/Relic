package com.relic.presentation.displaypost.list

import android.support.v7.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.FullPostView
import com.relic.presentation.util.MediaHelper

class FullPostVH(
    private val fullPost : FullPostView
) : RecyclerView.ViewHolder(fullPost) {

    fun bindPost(post : PostModel) {
        fullPost.setPost(post)
    }

    fun initializeOnClicks(delegate : DisplayPostContract.PostViewDelegate) {
        fullPost.setOnClicks(delegate)
    }

}