package com.relic.presentation.search

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.relic.data.PostSource
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.domain.models.UserModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.search.post.PostSearchResultsVM
import javax.inject.Inject


/**
 * this fragment will allow the user to search everything
 * TODO add user, sub, post, and comment search vm to this vm as delegates
 */
class SearchResultsFragment : RelicFragment() {
    @Inject
    lateinit var factory : PostSearchResultsVM.Factory

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    private val searchResultsVM: PostSearchResultsVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {

                return factory.create(postSource) as T
            }
        }).get(PostSearchResultsVM::class.java)
    }

    // we use all since we don't want to restrict search
    private val postSource = PostSource.All

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)
        searchResultsVM.apply {
        }
    }

    private fun handleSubResults(results : List<SubredditModel>) {
    }

    private fun handlePostResults(results : List<PostModel>) {
    }

    private fun handleUserResults(results : List<UserModel>) {
    }
}