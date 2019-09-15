package com.relic.presentation.search

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import com.relic.data.PostSource
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.domain.models.UserModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import javax.inject.Inject



class SearchResultsFragment : RelicFragment() {
    @Inject
    lateinit var factory : SearchResultsVM.Factory

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    val searchResultsVM: SearchResultsVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {

                return factory.create(postSource) as T
            }
        }).get(SearchResultsVM::class.java)
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