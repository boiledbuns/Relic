package com.relic.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.UserModel
import com.relic.interactor.Contract
import com.relic.preference.PostViewPreferences
import com.relic.presentation.base.ItemNotifier
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.search.subreddit.view.SearchSubPreviewView
import kotlinx.android.synthetic.main.sub_search_preview_item.view.*

sealed class ResultType(
    val type : Int
) {
    object SUB: ResultType(1)
    object POST : ResultType(2)
//    object USER: ResultType()
}

class SearchResultsAdapter(
    var currentType: ResultType,
    private val subSearchInteractor: SubredditSearchDelegate,
    private val postInteractor: Contract.PostAdapterDelegate,
    private val viewPrefsManager: PostViewPreferences
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var subSearchResults: List<SubPreviewModel> = emptyList()
    private var postSearchResults: List<PostModel> = emptyList()
    private var userSearchResults: List<UserModel> = emptyList()

    override fun getItemCount(): Int = when (currentType) {
        ResultType.SUB -> subSearchResults.size
        ResultType.POST -> postSearchResults.size
//        ResultType.USER -> userSearchResults.size
    }

    override fun getItemViewType(position: Int): Int {
        return currentType.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ResultType.SUB.type -> {
                val subPreviewView = SearchSubPreviewView(parent.context)
                SubPreviewVH(subPreviewView)
            }
           else -> {
                val postView = RelicPostItemView(parent.context, postLayout = viewPrefsManager.getPostCardStyle())
                PostItemVH(postView)
            }
//            ResultType.USER -> { }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ResultType.SUB.type -> {
                (holder as SubPreviewVH).bindSubredditName(subSearchResults[position])
            }
            else -> {
                (holder as PostItemVH).bindPost(postSearchResults[position])
            }
//            ResultType.USER -> { }
        }

    }

    fun clear() {
        subSearchResults = emptyList()
        postSearchResults= emptyList()
        userSearchResults = emptyList()
        notifyDataSetChanged()
    }

    fun updateSearchResults(
        newSubSearchResults: List<SubPreviewModel>? = null,
        newPostSearchResults: List<PostModel>? = null,
        newUserSearchResults: List<UserModel>? = null
    ) {
        newSubSearchResults?.let {
            currentType = ResultType.SUB
            subSearchResults = it
        }
        newPostSearchResults?.let {
            currentType = ResultType.POST
            postSearchResults = it
        }
//        newUserSearchResults?.let {
//            currentType = ResultType.USER
//            userSearchResults = it
//        }
        notifyDataSetChanged()
    }

    // region viewholders
    inner class SubPreviewVH(
        private val view: SearchSubPreviewView
    ) : RecyclerView.ViewHolder(view) {

        fun bindOnClick() {
            view.apply {
                root.setOnClickListener {
                    subSearchInteractor.visit(subSearchResults[absoluteAdapterPosition].name)
                }

                root.setOnLongClickListener {
                    subSearchInteractor.preview(subSearchResults[absoluteAdapterPosition].name)
                    true
                }
            }
        }

        fun bindSubredditName(name: SubPreviewModel) {
            view.bind(name)
        }
    }

    inner class PostItemVH(
        private val postItemView: RelicPostItemView
    ) : RecyclerView.ViewHolder(postItemView), ItemNotifier {

        override fun notifyItem() {
            notifyItemChanged(layoutPosition)
        }

        init {
            postItemView.setViewDelegate(postInteractor, this)
        }

        fun bindPost(postModel: PostModel) = postItemView.setPost(postModel)
    }
    // endregion viewholders
}