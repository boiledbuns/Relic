package com.relic.presentation.displaysubs

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.relic.R
import com.relic.presentation.adapter.PinnedSubItemAdapter
import kotlinx.android.synthetic.main.display_pinned_subs.view.*

class DisplayPinnedSubsView (
        context : Context,
        attrs: AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    lateinit var delegate : DisplaySubsContract.VM

    init {
        inflate(getContext(), R.layout.display_pinned_subs, this)
        pinnedSubsRecyclerView.adapter = PinnedSubItemAdapter()
    }
}