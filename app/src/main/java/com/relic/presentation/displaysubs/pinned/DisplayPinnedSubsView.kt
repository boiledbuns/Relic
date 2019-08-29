package com.relic.presentation.displaysubs.pinned

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.SubredditModel
import kotlinx.android.synthetic.main.display_pinned_subs.view.*

class DisplayPinnedSubsView @JvmOverloads constructor(
        context : Context,
        attrs: AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val pinnedSubAdapter = PinnedSubItemAdapter()

    init {
        LayoutInflater.from(context).inflate(R.layout.display_pinned_subs, this, true)
        pinnedSubsRecyclerView.apply {
            adapter = pinnedSubAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    fun setPinnedSubreddits(pinnedSubs : List<SubredditModel>) {
        pinnedSubsCountTextView.text = resources.getString(R.string.pinned_subs_count, pinnedSubs.size)
        pinnedSubAdapter.setPinnedSubreddits(pinnedSubs)
    }
}