package com.relic.presentation.customview

import android.content.Context
import androidx.appcompat.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import kotlinx.android.synthetic.main.relic_toolbar.view.*

class RelicToolbar @JvmOverloads constructor(
    context: Context,
    attrs : AttributeSet? = null,
    defStyleAttr : Int = 0
): Toolbar (context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.relic_toolbar, this, true)
    }

    fun setSubtitleTitle(resId: Int) {
        my_toolbar_title.text = resources.getString(resId)
    }
}