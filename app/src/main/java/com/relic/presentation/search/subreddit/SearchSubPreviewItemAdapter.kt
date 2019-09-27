package com.relic.presentation.search.subreddit

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.SubPreviewModel
import com.relic.presentation.search.SubredditSearchDelegate
import com.relic.presentation.search.subreddit.view.SearchSubPreviewView
import kotlinx.android.synthetic.main.sub_search_preview_item.view.*

class SearchSubPreviewItemAdapter (
    private val subSearchDelegate : SubredditSearchDelegate
): RecyclerView.Adapter<SearchSubPreviewItemAdapter.SearchSubNameVH>() {
    private var searchResults: List<SubPreviewModel> = emptyList()

    override fun getItemCount(): Int = searchResults.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSubNameVH {
        val searchSubnameView = SearchSubPreviewView(parent.context)
        return SearchSubNameVH(searchSubnameView).apply {
            bindOnClick()
        }
    }

    override fun onBindViewHolder(holder: SearchSubNameVH, position: Int) {
        holder.bindSubredditName(searchResults[position])
    }

    fun updateSearchResults(newSearchResults: List<SubPreviewModel>) {
        searchResults = newSearchResults
        notifyDataSetChanged()
    }

    inner class SearchSubNameVH(
        private val view : SearchSubPreviewView
    ) : RecyclerView.ViewHolder(view) {

        fun bindOnClick() {
            view.apply {
                root.setOnClickListener {
                    subSearchDelegate.visit(searchResults[adapterPosition].name)
                }

                root.setOnLongClickListener {
                    subSearchDelegate.preview(searchResults[adapterPosition].name)
                    true
                }
            }
        }

        fun bindSubredditName(name : SubPreviewModel) {
            view.bind(name)
        }
    }
}

