package com.relic.presentation.search.subs

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SearchSubNameItemAdapter : RecyclerView.Adapter<SearchSubNameItemAdapter.SearchSubNameVH>() {
    private var searchResults: List<String> = emptyList()

    override fun getItemCount(): Int = searchResults.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSubNameVH {
        val searchSubnameView = SearchSubnameView(parent.context)
        return SearchSubNameVH(searchSubnameView)
    }

    override fun onBindViewHolder(holder: SearchSubNameVH, position: Int) {
        holder.bindSubredditName(searchResults[position])
    }

    fun updateSearchResults(newSearchResults: List<String>) {
        searchResults = newSearchResults
        notifyDataSetChanged()
    }

    fun clearSearchResults() {
        searchResults = emptyList()
        notifyDataSetChanged()
    }

    inner class SearchSubNameVH(
        private val view : SearchSubnameView
    ) : RecyclerView.ViewHolder(view) {

        fun bindSubredditName(name : String) {
            view.bindName(name)
        }
    }
}

