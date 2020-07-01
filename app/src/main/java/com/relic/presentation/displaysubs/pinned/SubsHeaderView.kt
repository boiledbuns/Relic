package com.relic.presentation.displaysubs.pinned

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.relic.R
import com.relic.data.PostSource
import com.relic.domain.models.SubredditModel
import kotlinx.android.synthetic.main.primary_sources_view.view.*
import kotlinx.android.synthetic.main.subs_header_view.view.*

class SubsHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val delegate: (postSource : PostSource) -> Unit
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val favSubsAdapter = FavSubItemAdapter()

    init {
        LayoutInflater.from(context).inflate(R.layout.subs_header_view, this, true)
        pinnedSubsRecyclerView.apply {
            adapter = favSubsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        default_sources.source_frontpage.setOnClickListener {
            delegate(PostSource.Frontpage)
        }
        default_sources.source_all.setOnClickListener {
            delegate(PostSource.All)
        }
        default_sources.source_popular.setOnClickListener {
            delegate(PostSource.Popular)
        }
    }

    fun setPinnedSubreddits(pinnedSubs: List<SubredditModel>) {
        pinnedSubsCountTextView.text = resources.getString(R.string.fav_subs_count, pinnedSubs.size)
        favSubsAdapter.setFavSubreddits(pinnedSubs)
    }
}