package com.relic.presentation.displayuser

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.UtilModule
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user_preview.*
import kotlinx.android.synthetic.main.display_user_preview.view.*

class DisplayUserPreview : BottomSheetDialogFragment() {

    private lateinit var displayUserVM : DisplayUserVM
    private lateinit var username : String

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_USERNAME)?.let { username = it }

        displayUserVM = ViewModelProviders.of(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent.builder()
                    .repoModule(RepoModule(context!!))
                    .authModule(AuthModule(context!!))
                    .utilModule(UtilModule(activity!!.application))
                    .build()
                    .getDisplayUserVM().create(username) as T
            }
        }).get(DisplayUserVM::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user_preview, container, false).apply {
            userPreviewUser.text = resources.getString(R.string.user_prefix_label, username)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel(viewLifecycleOwner)
    }

    // endregion lifecycle hooks

    private fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        displayUserVM.userLiveData.nonNull().observe(lifecycleOwner) { userPreviewUserPreview.setUser(it) }
    }

    companion object {
        val ARG_USERNAME = "username"

        fun create(username  : String) : DisplayUserPreview {
            val bundle = Bundle()
            bundle.putString(ARG_USERNAME, username)

            return DisplayUserPreview().apply {
                arguments = bundle
            }
        }
    }
}