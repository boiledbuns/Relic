package com.relic.presentation.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.domain.models.SubredditModel
import kotlinx.android.synthetic.main.pinned_sub_item.view.*

class PinnedSubItemAdapter : RecyclerView.Adapter <PinnedSubItemAdapter.PinnedSubVH> () {

    class PinnedSubVH(private val subItemView : View) : RecyclerView.ViewHolder(subItemView) {
        fun bind (sub : SubredditModel) {
            subItemView.apply {
                subNameTextView.text = subItemView.context.resources.getString(R.string.sub_prefix_name, sub.subName)
                subCountTextView.text = subItemView.context.resources.getString(R.string.subscriber_count, sub.subscriberCount)
            }
        }
    }

    var pinnedSubs : List <SubredditModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PinnedSubItemAdapter.PinnedSubVH {
        val viewHolder = PinnedSubVH(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.pinned_sub_item, parent, false))

        viewHolder.bind(pinnedSubs[position])

        return viewHolder
    }

    override fun getItemCount(): Int {
        return pinnedSubs.size
    }

    override fun onBindViewHolder(viewHolder: PinnedSubVH, position: Int) {
        viewHolder.bind(pinnedSubs[position])
    }

    fun setPinnedSubreddits(newPinnedSubs : List<SubredditModel>) {
        this.pinnedSubs = newPinnedSubs
        notifyDataSetChanged()
    }
}