package com.relic.presentation.displaysubs.list

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.SubredditModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.sub_item.view.*
import timber.log.Timber

class SubItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.sub_item, this, true)
    }

    fun bind(subredditModel: SubredditModel) {
        sub_name.text = subredditModel.subName
        subredditModel.iconUrl?.let {
            if (it.isNotEmpty()) {
                try {
                    Picasso.get().load(it).into(sub_icon)
                } catch (e: Error) {
                    Timber.d("error loading image $e")
                }
            }
        }
    }
}