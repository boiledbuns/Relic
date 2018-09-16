package com.relic.presentation.editor

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.relic.R
import com.relic.data.CommentRepositoryImpl
import com.relic.data.PostRepositoryImpl
import kotlinx.android.synthetic.main.editor.*

class EditorView : Fragment() {
    companion object {
        const val NAME_KEY = "fullname"
        const val SUB_NAME_KEY = "subreddit"
        const val PARENT_TYPE_KEY = "post_type"
    }

    private lateinit var viewModel : EditorContract.VM
    private lateinit var toolbar : Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.editor, container, false)

        toolbar = contentView.findViewById(R.id.reply_post_toolbar) as Toolbar
        toolbar.title = "Replying to post"

        return contentView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(EditorVM::class.java)
        if (!viewModel.isInitialized()) {
            val subName : String? = arguments?.getString(SUB_NAME_KEY)
            val fullName : String? = arguments?.getString(NAME_KEY)
            val parentType : Int? = arguments?.getInt(PARENT_TYPE_KEY)

            if (subName != null && fullName != null && parentType != null) {
                viewModel.let {
                    it.init(subName, fullName, parentType, PostRepositoryImpl(context), CommentRepositoryImpl(context))
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