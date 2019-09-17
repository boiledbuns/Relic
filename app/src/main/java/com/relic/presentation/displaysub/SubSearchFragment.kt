package com.relic.presentation.displaysub

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.relic.R
import com.relic.presentation.main.RelicError
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.domain.models.PostModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_sub_search.*
import javax.inject.Inject

class SubSearchFragment : RelicFragment() {
    @Inject
    lateinit var factory : DisplaySubVM.Factory
    private val subSearchVM : DisplaySubVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create(source) as T
            }
        }).get(DisplaySubVM::class.java)
    }

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    private lateinit var source : PostSource
    private lateinit var postAdapter: PostItemAdapter

    private var scrollLocked = true
    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        source = arguments?.getParcelable(ARG_SOURCE) as PostSource
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_sub_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postAdapter = PostItemAdapter(viewPrefsManager, subSearchVM)
        subSearchRV.apply {
            adapter = postAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }

        initSearchEditText(subSearch)
        subSearch.requestFocus()

        getSystemService(requireContext(), InputMethodManager::class.java)
            ?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN)

        bindViewModel(this)
        attachScrollListeners()
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        subSearchVM.errorLiveData.nonNull().observe(lifecycleOwner){ handleError(it) }
        subSearchVM.searchResults.nonNull().observe(lifecycleOwner){ handleSearchResults(it) }
        subSearchVM.subNavigationLiveData.observe(lifecycleOwner){  }
    }

    private fun attachScrollListeners() {
        subSearchRV.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // checks if the recycler view can no longer scroll downwards
                if (!recyclerView.canScrollVertically(1) && !scrollLocked) {
                    // lock scrolling until set of posts are loaded to prevent additional unwanted retrievals
                    scrollLocked = true
                    displaySearchProgress.visibility = View.VISIBLE

                    // fetch the next post listing
                    subSearchVM.retrieveMoreSearchResults()
                }
            }
        })
    }

    private fun handleSearchResults(results : List<PostModel>) {
        subSearchResultCount.text = getString(R.string.search_sub_result_count, results.size)
        postAdapter.setPostList(results)

        // hide loading and allow load more again
        scrollLocked = false
        displaySearchProgress.visibility = View.GONE
    }

    private fun initSearchEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // TODO refine search experience -> basic idea is to start only search after user stops typing
                subSearchVM.search(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun handleError(error : RelicError) {
        displaySearchProgress.visibility = View.GONE
        when(error) {
            is NoResults -> {
                Toast.makeText(context, "No more results for query", Toast.LENGTH_SHORT).show()
            }
            is RelicError.NetworkUnavailable -> {
                Snackbar.make(
                    subSearchRoot,
                    resources.getString(R.string.network_unavailable),
                    Snackbar.LENGTH_INDEFINITE
                ).apply {
                    setAction(resources.getString(R.string.refresh)) {
                        subSearchVM.search(subSearch.text.toString())
                    }
                    show()
                }
            }
        }
    }

    // endregion lifecycle hooks

    companion object {
        val ARG_SOURCE = "post_source"

        fun create(source  : PostSource) : SubSearchFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARG_SOURCE, source)

            return SubSearchFragment().apply {
                arguments = bundle
            }
        }
    }
}