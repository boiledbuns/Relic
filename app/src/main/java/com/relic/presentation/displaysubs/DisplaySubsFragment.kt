package com.relic.presentation.displaysubs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
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

        display_subs_toolbar.apply {
            inflateMenu(R.menu.display_subs_menu)
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.display_subs_refresh) {
                    // refresh the current list and retrieve more posts
                    viewModel.refreshSubs()
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }
            }
        }
        // attach the actions associated with loading the posts
        attachScrollListeners()
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun handleNavReselected(): Boolean {
        return when (display_subs_recyclerview.canScrollVertically(-1)) {
            true -> {
                // can still scroll up, so reselection should scroll to top
                display_subs_recyclerview.smoothScrollToPosition(0)
                true
            }
            false -> {
                // already at top, we don't handle
                false
            }
        }
    }

    // region view model binding and handlers
    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        // allows the list to be updated as subreddits are retrieved from the network
        viewModel.subscribedSubsList.observe(lifecycleOwner) { subs ->
            subs?.let {
                subAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
                subAdapter.setList(ArrayList(it))
            }
        }

        viewModel.pinnedSubs.observe(lifecycleOwner) { pinnedSubs ->
            pinnedSubs?.let {
                subAdapter.setPinnedSubs(it)
            }
        }

        viewModel.isLoadingLiveData.observe(lifecycleOwner) {
            it?.let { isLoading ->
                val visibility = if (isLoading) View.VISIBLE else View.GONE
                displaySubsProgress.visibility = visibility
            }
        }
    }

    // endregion view model binding and handlers

    private fun attachScrollListeners() {
        // set up fast scroller
        fastscroller.setupWithRecyclerView(
            recyclerView = display_subs_recyclerview,
            getItemIndicator = { position ->
                when (position) {
                    // first item is always the header view
                    0 -> {
                        FastScrollItemIndicator.Icon(R.drawable.ic_star)
                    }
                    else -> {
                        val item = subAdapter.getList()[position - 1]
                        FastScrollItemIndicator.Text(item.subName.substring(0, 1).toUpperCase())
                    }
                }
            }
        )
    }
}
