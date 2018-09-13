package com.relic.presentation.editor

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.relic.R

class EditorView : Fragment() {
    companion object {
        @JvmField
        val BUNDLE_KEY = "fullname"
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
        if (viewModel.isInitialized()) {
            // TODO initialize viewmodel (start using di with dagger 2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }



}