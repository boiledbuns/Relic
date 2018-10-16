package com.relic.presentation.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.data.models.SubredditModel
import kotlinx.android.synthetic.main.pinned_sub_item.view.*

class PinnedSubItemAdapter : RecyclerView.Adapter <PinnedSubItemAdapter.PinnedSubVH> () {

    class PinnedSubVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bind (sub : SubredditModel) {
            itemView.subNameTextView.text = sub.name
            itemView.subCountTextView.text = sub.subscriberCount.toString()
        }
    }

    var pinnedSubs : List <SubredditModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PinnedSubItemAdapter.PinnedSubVH {
        val viewHolder = PinnedSubVH(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.pinned_sub_item, parent, true))

        viewHolder.bind(pinnedSubs.get(position))

        return viewHolder
    }

    override fun getItemCount(): Int {
        return pinnedSubs.size
    }

    override fun onBindViewHolder(viewHolder: PinnedSubVH, position: Int) {
        viewHolder.bind(pinnedSubs.get(position))
    }

    fun setPinnedSubreddits(pinnedSubs : List<SubredditModel>) {
        this@PinnedSubItemAdapter.pinnedSubs = pinnedSubs
    }
}