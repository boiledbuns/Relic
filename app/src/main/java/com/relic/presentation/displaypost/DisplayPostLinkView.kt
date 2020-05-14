package com.relic.presentation.displaypost

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.display_post_link.view.*
import timber.log.Timber

class DisplayPostLinkView @JvmOverloads constructor(
        context : Context,
        attrs: AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.subs_header_view, this, true)
    }

    fun loadLinkPreview(thumbnailUrl : String) {
        postLinkUrl.text = thumbnailUrl
        postLinkThumbnail

        try {
            Picasso.get().load(thumbnailUrl).fit().centerCrop().into(postLinkThumbnail)
        }
        catch (error: Error) {
            Timber.e("Issue loading image %s", error.toString())
            // TODO load an empty image into the thumbnail
        }
    }


}