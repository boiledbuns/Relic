package com.relic.presentation.preferences.appearance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.preference.POST_LAYOUT_CARD
import com.relic.preference.POST_LAYOUT_SPAN
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.preferences.PreferenceChangedListener
import kotlinx.android.synthetic.main.preferences_post_layout.*
import javax.inject.Inject

class PostLayoutFragment : RelicFragment() {
    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    private lateinit var preferenceChangedListener: PreferenceChangedListener

    private lateinit var previewPost : PostModel
    private var currentLayout : Int = POST_LAYOUT_SPAN

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        previewPost = ViewPreferencesHelper.initializePreviewPost(resources)

        return inflater.inflate(R.layout.preferences_post_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postLayoutToolbarView.setOnClickListener { activity!!.onBackPressed() }

        currentLayout = viewPrefsManager.getPostCardStyle()

        // initialize the adapter for the spinner displaying post layout options
        val layoutSpinnerAdapter = ArrayAdapter.createFromResource(
                context!!,
                R.array.post_layout_options,
                android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        layoutSpinnerView.apply {
            adapter = layoutSpinnerAdapter

            // we have to do this because spinner auto calls onItemSelected otherwise
            setSelection(currentLayout)
            onItemSelectedListener = PostLayoutListener()
        }

        resetPostPreviewView()
    }

    private fun onLayoutSelected(position: Int) {
        val selectedLayout = when (position) {
            1 -> POST_LAYOUT_CARD
            else -> POST_LAYOUT_SPAN
        }

        // only update post if layout not currently selected
        if (currentLayout != selectedLayout) {
            viewPrefsManager.setPostCardStyle(selectedLayout)
            currentLayout = selectedLayout

            resetPostPreviewView()
        }
    }

    private fun resetPostPreviewView() {
        val postItemView = RelicPostItemView(context!!, postLayout = viewPrefsManager.getPostCardStyle())

        postItemView.setPost(previewPost)

        previewPostLayoutFrameView.apply {
            removeAllViews()
            addView(postItemView)
        }
    }

    inner class PostLayoutListener : AdapterView.OnItemSelectedListener{
        override fun onNothingSelected(p0: AdapterView<*>?) {}

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            when(p0?.id) {
                layoutSpinnerView.id -> onLayoutSelected(p2)
            }
        }

    }

    companion object {

        fun create(listener : PreferenceChangedListener): PostLayoutFragment {
            return PostLayoutFragment().apply {
                preferenceChangedListener = listener
            }
        }
    }
}