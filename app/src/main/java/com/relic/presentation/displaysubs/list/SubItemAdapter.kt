package com.relic.presentation.displaysubs.list

import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.SubredditModel
import com.relic.interactor.Contract.SubAdapterDelegate
import com.relic.interactor.SubInteraction
import com.relic.presentation.displaysubs.list.SubItemAdapter.SubItemVH
import java.util.*

class SubItemAdapter(
    private val subAdapterDelegate: SubAdapterDelegate
) : RecyclerView.Adapter<SubItemVH>() {
    private var subList: MutableList<SubredditModel> = ArrayList()

    inner class SubItemVH(var subItemView: SubItemView) : RecyclerView.ViewHolder(subItemView) {
        fun bind(subModel: SubredditModel?) {
            subItemView.bind(subModel!!)
        }

        init {
            subItemView.setOnClickListener { v: View? -> subAdapterDelegate.interact(subList[adapterPosition], SubInteraction.Visit) }
            subItemView.setOnLongClickListener { v: View? ->
                subAdapterDelegate.interact(
                    subList[adapterPosition],
                    SubInteraction.Preview
                )
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubItemVH {
        val subItemView = SubItemView(parent.context)
        return SubItemVH(subItemView)
    }

    override fun onBindViewHolder(holder: SubItemVH, position: Int) {
        holder.bind(subList[position])
    }

    override fun getItemCount(): Int {
        return subList.size
    }

    /**
     *
     * @param newSubs list of all posts
     */
    fun setList(newSubs: MutableList<SubredditModel>) {
        calculateDiff(newSubs).dispatchUpdatesTo(this)
        subList = newSubs
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

interface NavDelegate {
    fun onNavigate(sub: SubredditModel?)
}