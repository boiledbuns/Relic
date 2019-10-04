package com.relic.presentation.subsyncconfig

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.data.PostSource
import com.relic.presentation.base.RelicFragment

class SubSyncConfigFragment : RelicFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.config_sub_sync, container, false)
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