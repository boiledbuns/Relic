package com.relic.presentation.displaysubs.pinned

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.relic.R
import com.relic.domain.models.SubredditModel
import kotlinx.android.synthetic.main.fav_sub_item.view.*

class FavSubItemAdapter : RecyclerView.Adapter <FavSubItemAdapter.FavSubVH> () {

    class FavSubVH(private val subItemView : View) : RecyclerView.ViewHolder(subItemView) {
        fun bind (sub : SubredditModel) {
            subItemView.apply {
                subNameTextView.text = subItemView.context.resources.getString(R.string.sub_prefix_name, sub.subName)
                subCountTextView.text = subItemView.context.resources.getString(R.string.subscriber_count, sub.subscriberCount)
            }
        }
    }

    var favSubs : List <SubredditModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): FavSubVH {
        val viewHolder = FavSubVH(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.fav_sub_item, parent, false))

        viewHolder.bind(favSubs[position])

        return viewHolder
    }

    override fun getItemCount(): Int {
        return favSubs.size
    }

    override fun onBindViewHolder(viewHolder: FavSubVH, position: Int) {
        viewHolder.bind(favSubs[position])
    }

    fun setFavSubreddits(newSubs : List<SubredditModel>) {
        this.favSubs = newSubs
        notifyDataSetChanged()
    }
}