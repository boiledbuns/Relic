package com.relic.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R

class RelicPostItem @JvmOverloads constructor(
        context: Context,
        attrs : AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // TODO add viewdelegate for handling view options

    init {
        LayoutInflater.from(context).inflate(R.layout.post_item_span, this, true)
    }

}