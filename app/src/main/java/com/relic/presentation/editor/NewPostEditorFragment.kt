package com.relic.presentation.editor

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.relic.R
import com.relic.presentation.base.RelicFragment
import kotlinx.android.synthetic.main.editor_new_post.view.*
import javax.inject.Inject

class NewPostEditorFragment : RelicFragment() {

    @Inject
    lateinit var factory : NewPostEditorVM.Factory

    private val newPostEditorVM : NewPostEditorVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor ViewModel
                return factory.create(subName) as T
            }
        }).get(NewPostEditorVM::class.java)
    }

    lateinit var subName : String

    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            getString(SUB_NAME_ARG)?.let { subName = it } ?: dismiss()
        } ?: dismiss()

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.editor_new_post, container, false).apply {
            val pActivity = (activity as AppCompatActivity)

            toolbar = reply_post_toolbar as Toolbar
            toolbar.title = subName

            pActivity.setSupportActionBar(toolbar)

            editorNewPostTitle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    newPostEditorVM.onTitleChanged(editable.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            editorNewPostBody.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    newPostEditorVM.onBodyChanged(editable.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        menu?.clear()
        inflater?.inflate(R.menu.editor_new_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true

        when (item?.itemId) {
            R.id.editor_new_post_submit -> {
                newPostEditorVM.submit()
            }
            R.id.editor_new_post_save -> {
                newPostEditorVM.saveChanges()
            }
            else -> override = super.onOptionsItemSelected(item)
        }

        return override
    }

    companion object {
        private const val SUB_NAME_ARG = "arg_subreddit_name"

        fun create(subreddit: String) : NewPostEditorFragment {
            return NewPostEditorFragment().apply {
                arguments = Bundle().apply {
                    putString(SUB_NAME_ARG, subreddit)
                }
            }
        }
    }
}