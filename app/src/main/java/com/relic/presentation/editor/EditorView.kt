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
        const val FULLNAME_ARG = "fullname"
        const val SUBNAME_ARG = "subreddit"
        const val PARENT_TYPE_KEY = "post_type"
    }

    private val viewModel : EditorVM by lazy {
        initializeVM()
    }

    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val subName : String? = arguments?.getString(SUBNAME_ARG)
        val fullName : String? = arguments?.getString(FULLNAME_ARG)
        val parentType : Int? = arguments?.getInt(PARENT_TYPE_KEY)

        if (subName != null && fullName != null && parentType != null) {
            viewModel.init(subName, fullName, parentType)
        }

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

    private fun initializeVM() : EditorVM {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor VM
                return DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .authModule(AuthModule(context!!))
                        .build()
                        .getEditorVM().create() as T
            }
        }).get(EditorVM::class.java)
    }

    private fun bindVM() {
        viewModel.parentModel.nonNull().observe(this) {
            parentTitleTextView.text = it.title

            if (it.body == null || it.body.isEmpty()) {
                parentBodyTextView.text = resources.getString(R.string.empty_parent_body)
            } else {
                parentBodyTextView.text = it.body
            }
        }
    }
}