package com.relic.presentation.search.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.displayuser.DisplayUserFragment
import com.relic.presentation.helper.SearchInputCountdown
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.UserSearchOptions
import com.relic.presentation.search.UserSearchResults
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

    private val countDownTimer = SearchInputCountdown()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userSearch.initSearchWidget()
    }

    private fun SearchView.initSearchWidget() {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                countDownTimer.cancel()
                countDownTimer.start {
                    val searchOptions = generateSearchOptions()
                    userSearchVM.search(searchOptions)
                }
                userSearchVM.updateQuery(newText.toString())

                // action is handled by listener
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                clearFocus()
                userSearchVM.openUser(query)

                // action is handled by listener
                return true
            }
        })
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        super.bindViewModel(lifecycleOwner)

        userSearchVM.apply {
            errorLiveData.observe(lifecycleOwner) { handleError(it) }
            searchResultsLiveData.nonNull().observe(lifecycleOwner) { handleSearchResults(it) }
            navigationLiveData.nonNull().observe(lifecycleOwner) { handleNavigation(it) }
        }
    }

    private fun handleSearchResults(searchResults : UserSearchResults) {
        searchResults.user?.let { user ->
            userNotFound.visibility = View.INVISIBLE

            userPreviewContainer.apply {
                visibility = View.VISIBLE
                userSearchVM.search(generateSearchOptions())

                setOnClickListener {
                    userSearchVM.openUser()
                }
            }

            username.text = user.fullName
            userPreview.setUser(user)

        } ?: userNotFound.apply {
            userPreviewContainer.visibility = View.INVISIBLE

            if (searchResults.query.isNotEmpty())  {
                visibility = View.VISIBLE
                text = getString(R.string.user_search_result, searchResults.query)
            }
        }

    }

    private fun handleNavigation(navData: NavigationData) {
        when (navData) {
            is NavigationData.ToUser -> {
                val userFrag = DisplayUserFragment.create(navData.username)
                requireActivity().supportFragmentManager.beginTransaction()
                        .add(R.id.main_content_frame, userFrag)
                        .addToBackStack(TAG).commit()
            }
        }
    }

    private fun generateSearchOptions() : UserSearchOptions {
        return UserSearchOptions()
    }

    private fun handleError(error : RelicError?) {
        // TODO
    }

    companion object {
        fun create() : UserSearchFragment {
            return UserSearchFragment()
        }
    }
}