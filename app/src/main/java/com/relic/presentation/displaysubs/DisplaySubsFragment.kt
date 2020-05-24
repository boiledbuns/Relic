package com.relic.presentation.displaysubs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.relic.R
import com.relic.interactor.Contract
import com.relic.network.NetworkUtil
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysubs.list.SubItemAdapter
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_subs.*
import java.util.*
import javax.inject.Inject

class DisplaySubsFragment : RelicFragment() {
    @Inject
    lateinit var factory: DisplaySubsVM.Factory

    @Inject
    lateinit var subredditInteractor: Contract.SubAdapterDelegate

    @Suppress("UNCHECKED_CAST")
    private val viewModel: DisplaySubsVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create() as T
            }
        }).get(DisplaySubsVM::class.java)
    }

    @Inject
    lateinit var networkUtil: NetworkUtil

    private lateinit var searchView: SearchView
    private lateinit var searchMenuItem: MenuItem

    private lateinit var subAdapter: SubItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_subs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subAdapter = SubItemAdapter(requireContext(), subredditInteractor).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        display_subs_recyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subAdapter
        }

        // attach the actions associated with loading the posts
        attachScrollListeners()
    }

    // region view model binding and handlers

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        // allows the list to be updated as subreddits are retrieved from the network
        viewModel.subscribedSubsList.observe(lifecycleOwner) { subs ->
            subs?.let {
                display_subs_swiperefreshlayout.isRefreshing = false
                subAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
                subAdapter.setList(ArrayList(it))
            }
        }

        viewModel.pinnedSubs.observe(lifecycleOwner) { pinnedSubs ->
            pinnedSubs?.let {
                subAdapter.setPinnedSubs(it)
            }
        }
    }

    // endregion view model binding and handlers

    private fun attachScrollListeners() {
        display_subs_swiperefreshlayout.setOnRefreshListener {
            // refresh the current list and retrieve more posts
            viewModel.refreshSubs()
        }
    }
}
