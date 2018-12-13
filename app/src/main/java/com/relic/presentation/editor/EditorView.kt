package com.relic.presentation.editor

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.presentation.base.RelicFragment
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.editor.*
import kotlinx.android.synthetic.main.editor.view.*

class EditorView : RelicFragment() {
    companion object {
        private const val FULLNAME_ARG = "arg_fullname"
        private const val SUB_NAME_ARG = "arg_subreddit_name"
        private const val PARENT_TYPE_KEY = "arg_post_type"

        fun create(subredditName : String, fullname : String, parentType : EditorContract.ParentType) : EditorView {
            return EditorView().apply {
                arguments = Bundle().apply {
                    putString(EditorView.SUB_NAME_ARG, subredditName)
                    putString(EditorView.FULLNAME_ARG, fullname)
                    putSerializable(EditorView.PARENT_TYPE_KEY, parentType)
                }
            }
        }
    }

    private val viewModel : EditorViewModel by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor ViewModel
                return DaggerVMComponent.builder()
                    .repoModule(RepoModule(context!!))
                    .authModule(AuthModule(context!!))
                    .build()
                    .getEditorVM().create(subName, fullName, parentType) as T
            }
        }).get(EditorViewModel::class.java)
    }

    lateinit var subName : String
    lateinit var fullName : String
    lateinit var parentType : EditorContract.ParentType

    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            getString(SUB_NAME_ARG)?.let { subName = it } ?: dismiss()
            getString(FULLNAME_ARG)?.let { fullName = it } ?: dismiss()
            (get(PARENT_TYPE_KEY) as EditorContract.ParentType?)?.let { parentType = it } ?: dismiss()
        } ?: dismiss()

        setHasOptionsMenu(true)
        bindVM()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.editor, container, false)

        toolbar = contentView.findViewById(R.id.reply_post_toolbar) as Toolbar
        toolbar.title = "Replying to post"

        contentView.replyEditorView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                viewModel.onTextChanged(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // do nothing
            }
        })

        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }
    }

    private fun bindVM() {
        viewModel.replyParentLiveData.nonNull().observe(this) {
            parentTitleTextView.text = it.title

            if (it.body == null || it.body.isEmpty()) {
                parentBodyTextView.text = resources.getString(R.string.empty_parent_body)
            } else {
                parentBodyTextView.text = it.body
            }
        }
    }
}