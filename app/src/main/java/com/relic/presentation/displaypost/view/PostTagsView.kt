package com.relic.presentation.displaypost.view

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.relic.R
import com.relic.domain.models.PostModel
import kotlinx.android.synthetic.main.post_tags.view.*

class PostTagsView @JvmOverloads constructor(
        context: Context,
        attrs : AttributeSet? = null,
        defStyleAttr : Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.post_tags, this, true)
    }

    fun setPostTags(postModel: PostModel) {
        //secondaryMetaTextview.text = resources.getString(R.string.user_prefix_label, postModel.author + " " + postModel.domain + " " + postModel.linkFlair)
        nsfwTag.visibility = if (postModel.nsfw) View.VISIBLE else View.GONE

        postTag.apply {
            if (postModel.linkFlair != null) {
                text = postModel.linkFlair
                // TODO replace with themes when adding proper theming
                @Suppress("DEPRECATION")
                background?.setTint(resources.getColor(R.color.discussion_tag))
                visibility = View.VISIBLE
            } else { visibility = View.GONE }
        }
    }
}