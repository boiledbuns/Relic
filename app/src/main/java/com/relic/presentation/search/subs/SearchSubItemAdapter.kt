package com.relic.presentation.search.subs

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.SubredditModel
import com.relic.presentation.adapter.SearchSubItemOnClick
import timber.log.Timber

class SearchSubItemAdapter(private val onclick: SearchSubItemOnClick) : RecyclerView.Adapter<SearchSubItemVH>() {
    private var searchResults: List<String> = emptyList()
    private var subscribedResults: List<SubredditModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): SearchSubItemVH {
        val searchItemView = SearchSubItemView(parent.context)
        return SearchSubItemVH(searchItemView)
    }

    override fun onBindViewHolder(searchItemVH: SearchSubItemVH, position: Int) {
        searchItemVH.bindSubreddit(subscribedResults[position])
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    fun clearSearchResults() {
        searchResults = emptyList()
        subscribedResults = emptyList()
        notifyDataSetChanged()
    }

    fun handleSearchResultsPayload(searchResults: List<String>) {
        if (searchResults != null) {
            Timber.d("Search results received %s", searchResults)
            this.searchResults = searchResults
            notifyDataSetChanged()
        }
    }

}
