package com.relic.presentation.displaysubs.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.RecyclerView
import com.relic.data.PostSource
import com.relic.domain.models.SubredditModel
import com.relic.interactor.Contract.SubAdapterDelegate
import com.relic.interactor.SubInteraction
import com.relic.presentation.base.RelicAdapter
import com.relic.presentation.displaysubs.list.SubItemAdapter.SubItemVH
import com.relic.presentation.displaysubs.pinned.SubsHeaderView
import kotlinx.coroutines.launch
import java.util.*

private const val TYPE_HEADER = 0
private const val TYPE_SUBS = 1

class SubItemAdapter(
    private val subAdapterDelegate: SubAdapterDelegate
) : RelicAdapter<RecyclerView.ViewHolder>() {
    private var subsHeaderView : SubsHeaderView? = null
    private var subList: MutableList<SubredditModel> = ArrayList()

    inner class SubItemVH(var subItemView: SubItemView) : RecyclerView.ViewHolder(subItemView) {
        fun bind(subModel: SubredditModel?) {
            subItemView.bind(subModel!!)
        }

        init {
            subItemView.setOnClickListener { v: View? ->
                subAdapterDelegate.interact(
                    PostSource.Subreddit(subList[absoluteAdapterPosition].subName),
                    SubInteraction.Visit)
            }
            subItemView.setOnLongClickListener { v: View? ->
                subAdapterDelegate.interact(
                    PostSource.Subreddit(subList[absoluteAdapterPosition].subName),
                    SubInteraction.Preview
                )
                true
            }
        }
    }

    inner class SubsHeaderVH(
        var subsHeaderView: SubsHeaderView
    ) : RecyclerView.ViewHolder(subsHeaderView)

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            else -> TYPE_SUBS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_HEADER -> {
                SubsHeaderView(parent.context) { postSource ->
                    subAdapterDelegate.interact(postSource, SubInteraction.Visit)
                }.run {
                    subsHeaderView = this
                    SubsHeaderVH(this)
                }
            }
            else -> {
                val subItemView = SubItemView(parent.context)
                SubItemVH(subItemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            TYPE_SUBS -> (holder as SubItemVH).bind(subList[position])
        }
    }

    override fun getItemCount(): Int {
        return subList.size
    }

    fun setList(newSubs: MutableList<SubredditModel>) {
        launch {
            calculateDiff(newSubs).dispatchUpdatesTo(this@SubItemAdapter)
            subList = newSubs
        }
    }

    fun setPinnedSubs(pinnedSubs: List<SubredditModel>) {
        subsHeaderView?.setPinnedSubreddits(pinnedSubs)
    }

    private fun calculateDiff(newSubs: List<SubredditModel>): DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return subList.size
            }

            override fun getNewListSize(): Int {
                return newSubs.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return subList[oldItemPosition].id == newSubs[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return true
            }
        })
    }

    fun clearList() {
        subList.clear()
        notifyDataSetChanged()
    }

}