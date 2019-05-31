package com.relic.presentation.displaysub

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.UtilModule
import com.relic.data.PostRepository
import com.relic.data.models.PostModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysub.list.PostItemAdapter
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_sub_search.*

class DisplaySubSearch : RelicFragment() {

    private lateinit var displaySubSearchVM : DisplaySubVM
    private lateinit var postAdapter: PostItemAdapter

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val source = arguments?.getParcelable(ARG_SOURCE) as PostRepository.PostSource

        displaySubSearchVM = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent.builder()
                    .repoModule(RepoModule(context!!))
                    .authModule(AuthModule(context!!))
                    .utilModule(UtilModule(activity!!.application))
                    .build()
                    .getDisplaySubVM().create(source) as T
            }
        }).get(DisplaySubVM::class.java)

        postAdapter = PostItemAdapter(displaySubSearchVM)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_sub_search, container, false).apply {
            (findViewById<RecyclerView>(R.id.subSearchRV)).apply {
                adapter = postAdapter
                layoutManager = LinearLayoutManager(context)
            }

            initSearchEditText((findViewById(R.id.subSearch)))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subSearch.requestFocus()

        getSystemService(requireContext(), InputMethodManager::class.java)
            ?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN)
        bindViewModel(this)
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        displaySubSearchVM.errorLiveData.observe(lifecycleOwner){ }
        displaySubSearchVM.searchResults.nonNull().observe(lifecycleOwner){ handleSearchResults(it) }
        displaySubSearchVM.subNavigationLiveData.observe(lifecycleOwner){  }
    }

    private fun handleSearchResults(results : List<PostModel>) {
        subSearchResultCount.text = getString(R.string.search_sub_result_count, results.size)
        postAdapter.setPostList(results)
    }

    private fun initSearchEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // TODO refine search experience -> basic idea is to start only search after user stops typing
                displaySubSearchVM.search(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // endregion lifecycle hooks

    companion object {
        val ARG_SOURCE = "post_source"

        fun create(source  : PostRepository.PostSource) : DisplaySubSearch {
            val bundle = Bundle()
            bundle.putParcelable(ARG_SOURCE, source)

            return DisplaySubSearch().apply {
                arguments = bundle
            }
        }
    }
}