package com.relic.presentation.subsyncconfig

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.relic.R
import com.relic.data.PostSource
import com.relic.presentation.base.RelicFragment
import kotlinx.android.synthetic.main.config_sub_sync.*

class SubSyncConfigFragment : RelicFragment() {

    private lateinit var postSource: PostSource

    private val subSyncPM by lazy {
        SubSyncPM(requireActivity().application, postSource.getSourceName())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            getParcelable<PostSource>(ARG_SUB_SOURCE)?.let {
                postSource = it
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.config_sub_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeToolbar()

        subSyncPM.bindPreferences()
        syncPostsTitle.text = getString(R.string.sync_sub_title, postSource.getSourceName())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun initializeToolbar() {
        val pActivity = (activity as AppCompatActivity)

        (sync_config_toolbar as Toolbar).apply {
            pActivity.setSupportActionBar(this)

            title = postSource.getSourceName()
            subtitle = getString(R.string.sync_config_menu)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }

        pActivity.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun SubSyncPM.bindPreferences() {
        postSyncEnabled.let {
            syncPostsToggle.isChecked = it
            syncPagesEdit.isEnabled = it
        }
        
        syncPagesEdit.apply {
            setSelection(postSyncPages - 1)
        }

        commentSyncEnabled.let {
            syncCommentsToggle.isChecked = it
        }
    }

    companion object {
        const val ARG_SUB_SOURCE = "arg_sub_source"

        fun create(postSource: PostSource) : SubSyncConfigFragment {
            return SubSyncConfigFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SUB_SOURCE, postSource)
                }
            }
        }
    }
}