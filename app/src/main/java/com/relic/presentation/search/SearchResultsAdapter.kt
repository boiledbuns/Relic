package com.relic.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.relic.data.PostSource
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.UserModel
import com.relic.interactor.Contract
import com.relic.interactor.SubInteraction
import com.relic.preference.PostViewPreferences
import com.relic.presentation.base.ItemNotifier
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.search.subreddit.view.SearchSubPreviewView
import com.relic.presentation.search.user.view.UserSearchItemView
import kotlinx.android.synthetic.main.sub_search_preview_item.view.*

class SearchResultsAdapter(
    var currentType: SearchResultType,
    private val subInteractor: Contract.SubAdapterDelegate,
    private val postInteractor: Contract.PostAdapterDelegate,
    private val userInteractor: Contract.UserAdapterDelegate,
    private val viewPrefsManager: PostViewPreferences
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var subSearchResults: List<SubPreviewModel> = emptyList()
    private var postSearchResults: List<PostModel> = emptyList()
    private var userSearchResults: List<UserModel> = emptyList()

    override fun getItemCount(): Int = when (currentType) {
        SearchResultType.SUB -> subSearchResults.size
        SearchResultType.POST -> postSearchResults.size
        SearchResultType.USER -> userSearchResults.size
    }

    fun setResultType(type: SearchResultType) {
        currentType = type
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return currentType.getType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SearchResultType.SUB.getType() -> {
                val subPreviewView = SearchSubPreviewView(parent.context)
                SubPreviewVH(subPreviewView)
            }
            SearchResultType.USER.getType() -> {
                val userSearchItemView = UserSearchItemView(parent.context)
                UserVH(userSearchItemView)
            }
            else -> {
                val postView = RelicPostItemView(parent.context, postLayout = viewPrefsManager.getPostCardStyle())
                PostItemVH(postView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            SearchResultType.SUB.getType() -> {
                (holder as SubPreviewVH).bindSubredditName(subSearchResults[position])
            }
            SearchResultType.USER.getType() -> {
                (holder as UserVH).bind(userSearchResults[position])
            }
            else -> {
                (holder as PostItemVH).bindPost(postSearchResults[position])
            }
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
            subSearchResults = it
        }
        newPostSearchResults?.let {
            postSearchResults = it
        }
        newUserSearchResults?.let {
            userSearchResults = it
        }
        notifyDataSetChanged()
    }

    // region viewholders
    inner class SubPreviewVH(
        private val view: SearchSubPreviewView
    ) : RecyclerView.ViewHolder(view) {

        init {
            view.apply {
                root.setOnClickListener {
                    val postSource = PostSource.Subreddit(subSearchResults[absoluteAdapterPosition].name)
                    subInteractor.interact(postSource, SubInteraction.Visit)
                }
                root.setOnLongClickListener {
                    val postSource = PostSource.Subreddit(subSearchResults[absoluteAdapterPosition].name)
                    subInteractor.interact(postSource, SubInteraction.Preview)
                    true
                }
            }
        }

        fun bindSubredditName(name: SubPreviewModel) {
            view.bind(name)
        }
    }

    inner class UserVH(
        private val userItemView : UserSearchItemView
    ) : RecyclerView.ViewHolder(userItemView) {
        init {
            userItemView.setViewDelegate(userInteractor)
        }

        fun bind(user: UserModel) {
            userItemView.bind(user)
        }
    }

    inner class PostItemVH(
        private val postItemView: RelicPostItemView
    ) : RecyclerView.ViewHolder(postItemView), ItemNotifier {
        init {
            postItemView.setViewDelegate(postInteractor, this)
        }

        override fun notifyItem() {
            notifyItemChanged(layoutPosition)
        }

        fun bindPost(postModel: PostModel) = postItemView.setPost(postModel)
    }
    // endregion viewholders
}

private fun SearchResultType.getType() = when (this) {
    SearchResultType.SUB -> 1
    SearchResultType.POST -> 2
    SearchResultType.USER -> 3
}