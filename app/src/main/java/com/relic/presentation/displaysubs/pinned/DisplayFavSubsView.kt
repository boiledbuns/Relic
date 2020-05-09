package com.relic.presentation.displaysubs.pinned

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.relic.R
import com.relic.domain.models.SubredditModel
import kotlinx.android.synthetic.main.display_fav_subs.view.*

class DisplayFavSubsView @JvmOverloads constructor(
        context : Context,
        attrs: AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val favSubsAdapter = FavSubItemAdapter()

    init {
        LayoutInflater.from(context).inflate(R.layout.display_fav_subs, this, true)
        favSubsRecyclerView.apply {
            adapter = favSubsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    fun setPinnedSubreddits(pinnedSubs : List<SubredditModel>) {
        favSubsCountTextView.text = resources.getString(R.string.fav_subs_count, pinnedSubs.size)
        favSubsAdapter.setFavSubreddits(pinnedSubs)
    }
}