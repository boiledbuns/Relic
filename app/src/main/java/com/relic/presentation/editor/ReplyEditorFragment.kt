package com.relic.presentation.editor

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import javax.inject.Inject

class ReplyEditorFragment : RelicFragment() {

    @Inject
    lateinit var factory : ReplyEditorVM.Factory

    private val replyEditorVM : ReplyEditorVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor ViewModel
                return factory.create(parent, parentIsPost) as T
            }
        }).get(ReplyEditorVM::class.java)
    }

    lateinit var parent : String
    var parentIsPost : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            getString(PARENT_ARG)?.let { parent = it } ?: dismiss()
            parentIsPost = getBoolean(IS_POST_ARG)
        } ?: dismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.editor_reply, container, false)
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        replyEditorVM.replyParentLiveData.nonNull().observe (this) {
//            parentTitleTextView.text = it.title
//
//            if (it.body == null || it.body.isEmpty()) {
//                parentBodyTextView.text = resources.getString(R.string.empty_parent_body)
//            } else {
//                parentBodyTextView.text = it.body
//            }
        }
    }

    companion object {
        private const val PARENT_ARG = "arg_parent_name"
        private const val IS_POST_ARG = "arg_post_name"

        fun create(parent: String, isPost : Boolean) : ReplyEditorFragment {
            return ReplyEditorFragment().apply {
                arguments = Bundle().apply {
                    putString(PARENT_ARG, parent)
                    putBoolean(IS_POST_ARG, isPost)
                }
            }
        }
    }
}