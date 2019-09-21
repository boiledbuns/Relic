package com.relic.presentation.search.subreddit

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.SubredditModel
import com.relic.presentation.search.subreddit.view.SearchSubItemView
import timber.log.Timber

class SearchSubItemAdapter: RecyclerView.Adapter<SearchSubItemAdapter.SearchSubItemVH>() {
    private var searchResults: List<SubredditModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSubItemVH {
        val searchItemView = SearchSubItemView(parent.context)
        return SearchSubItemVH(searchItemView)
    }

    override fun onBindViewHolder(vh: SearchSubItemVH, position: Int) {
        vh.bindSubreddit(searchResults[position])
    }

    override fun getItemCount(): Int = searchResults.size

    fun clearSearchResults() {
        searchResults = emptyList()
        notifyDataSetChanged()
    }

    fun updateSearchResults(searchResults: List<SubredditModel>) {
        Timber.d("Search results received %s", searchResults)
        this.searchResults = searchResults
        notifyDataSetChanged()
    }

    inner class SearchSubItemVH(
        val view : SearchSubItemView
    ) : RecyclerView.ViewHolder(view) {

        fun bindSubreddit(subModel : SubredditModel) {
            view.bind(subModel)
        }
    }

}
