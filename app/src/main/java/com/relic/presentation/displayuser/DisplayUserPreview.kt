package com.relic.presentation.displayuser

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.presentation.base.RelicBottomSheetDialog
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user_preview.*
import kotlinx.android.synthetic.main.display_user_preview.view.*
import javax.inject.Inject

class DisplayUserPreview : RelicBottomSheetDialog() {

    @Inject lateinit var factory : DisplayUserVM.Factory

    private val displayUserVM : DisplayUserVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.create(username) as T
            }
        }).get(DisplayUserVM::class.java)
    }
    private lateinit var username : String

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_USERNAME)?.let { username = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user_preview, container, false).apply {
            userPreviewUser.text = resources.getString(R.string.user_prefix_label, username)
            userPreviewUser.setOnClickListener {
                dismiss()

                val fullUserFrag = DisplayUserFragment.create(username)
                requireActivity().supportFragmentManager.beginTransaction()
                    .add(R.id.main_content_frame, fullUserFrag).addToBackStack(TAG).commit()
            }
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