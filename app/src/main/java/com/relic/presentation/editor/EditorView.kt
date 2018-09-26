package com.relic.presentation.editor

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.data.CommentRepositoryImpl
import com.relic.data.PostRepositoryImpl
import com.relic.dagger.DaggerRepositoryComponent
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.RepositoryComponent
import com.relic.dagger.VMComponent
import com.relic.data.CommentRepository
import com.relic.data.PostRepository
import com.relic.data.RepoModule
import com.relic.presentation.base.RelicFragment
import kotlinx.android.synthetic.main.editor.*
import javax.inject.Inject

class EditorView : RelicFragment() {
    companion object {
        const val NAME_KEY = "fullname"
        const val SUB_NAME_KEY = "subreddit"
        const val PARENT_TYPE_KEY = "post_type"
    }

    private lateinit var viewModel : EditorVM
    private lateinit var toolbar : Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.editor, container, false)

        toolbar = contentView.findViewById(R.id.reply_post_toolbar) as Toolbar
        toolbar.title = "Replying to post"

        return contentView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Inject
            lateinit var editorVM : EditorVM

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor VM
                DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .build()
                        .injectEditor(this)

                return editorVM as T
            }
        }).get(EditorVM::class.java)

        if (!viewModel.isInitialized()) {
            val subName : String? = arguments?.getString(SUB_NAME_KEY)
            val fullName : String? = arguments?.getString(NAME_KEY)
            val parentType : Int? = arguments?.getInt(PARENT_TYPE_KEY)

            if (subName != null && fullName != null && parentType != null) {
                viewModel.let {
                    it.init(subName, fullName, parentType)
                }
            }
        }

        observeVM()
    }

    private fun observeVM() {
        viewModel.getParentText().observe(this, Observer<String> {
            parentTextView.text = it
        })
    }

}