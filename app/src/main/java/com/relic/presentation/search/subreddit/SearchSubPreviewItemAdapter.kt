package com.relic.presentation.search.subreddit

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.SubPreviewModel
import com.relic.presentation.search.subreddit.view.SearchSubPreviewView

class SearchSubPreviewItemAdapter : RecyclerView.Adapter<SearchSubPreviewItemAdapter.SearchSubNameVH>() {
    private var searchResults: List<SubPreviewModel> = emptyList()

    override fun getItemCount(): Int = searchResults.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSubNameVH {
        val searchSubnameView = SearchSubPreviewView(parent.context)
        return SearchSubNameVH(searchSubnameView)
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

        fun bindSubredditName(name : SubPreviewModel) {
            view.bind(name)
        }
    }
}

