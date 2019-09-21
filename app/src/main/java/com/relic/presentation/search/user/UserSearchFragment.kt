package com.relic.presentation.search.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.relic.R
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.main.RelicError
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user_search.*
import javax.inject.Inject

class UserSearchFragment : RelicFragment() {
    @Inject
    lateinit var factory : UserSearchVM.Factory

    private val userSearchVM: UserSearchVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create() as T
            }
        }).get(UserSearchVM::class.java)
    }

    lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userAdapter = UserAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userResultsRV.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)

        userSearchVM.apply {
            errorLiveData.observe(lifecycleOwner) { handleError(it) }
            searchResults.nonNull().observe(lifecycleOwner) { handleSearchResults(it) }
        }
    }

    private fun handleError(error : RelicError?) {
        // TODO
    }

    private fun handleSearchResults(users : List<UserModel>) {
        userAdapter.updateResults(users)
        userResultsSize.text = getString(R.string.user_search_results_size, users.size)
    }

    companion object {
        fun create() : UserSearchFragment {
            return UserSearchFragment()
        }
    }
}