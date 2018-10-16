package com.relic.presentation.displaysubs

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.data.models.SubredditModel
import com.relic.presentation.adapter.PinnedSubItemAdapter
import kotlinx.android.synthetic.main.display_pinned_subs.view.*

class DisplayPinnedSubsView @JvmOverloads constructor(
        context : Context,
        attrs: AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val adapter = PinnedSubItemAdapter()

    init {
        LayoutInflater.from(context).inflate(R.layout.display_pinned_subs, this, true)
        pinnedSubsRecyclerView.adapter = adapter
        pinnedSubsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun setPinnedSubreddits(pinnedSubs : List<SubredditModel>) {
        adapter.setPinnedSubreddits(pinnedSubs)
    }
}