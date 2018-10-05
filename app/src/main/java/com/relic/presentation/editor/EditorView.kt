package com.relic.presentation.editor

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.data.RepoModule
import com.relic.presentation.base.RelicFragment
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.editor.*

class EditorView : RelicFragment() {
    companion object {
        const val NAME_KEY = "fullname"
        const val SUB_NAME_KEY = "subreddit"
        const val PARENT_TYPE_KEY = "post_type"
    }

    private val viewModel : EditorVM by lazy {
        initializeVM()
    }

    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val subName : String? = arguments?.getString(SUB_NAME_KEY)
        val fullName : String? = arguments?.getString(NAME_KEY)
        val parentType : Int? = arguments?.getInt(PARENT_TYPE_KEY)

        if (subName != null && fullName != null && parentType != null) {
            viewModel.let {
                it.init(subName, fullName, parentType)
            }
        }

        setHasOptionsMenu(true)
        bindVM()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.editor, container, false)

        toolbar = contentView.findViewById(R.id.reply_post_toolbar) as Toolbar
        toolbar.title = "Replying to post"

        return contentView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        
        toolbar.navigationIcon = resources.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
    }

    private fun initializeVM() : EditorVM {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor VM
                return DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .build()
                        .getEditorVMFactory().create() as T
            }
        }).get(EditorVM::class.java)
    }

    private fun bindVM() {
        viewModel.parentModel.nonNull().observe(this) {
            it.title?.let{ parentTitleTextView.text = it }

            if (it.body == null || it.body.isEmpty()) {
                parentBodyTextView.text = "This post has no text body"
            } else {
                parentBodyTextView.text = it.body
            }
        }
    }

}