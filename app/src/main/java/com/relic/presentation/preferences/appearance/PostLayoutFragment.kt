package com.relic.presentation.preferences.appearance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.preferences.PreferenceChangedListener
import kotlinx.android.synthetic.main.preferences_post_layout.*
import kotlinx.android.synthetic.main.preferences_theme.*
import javax.inject.Inject

class PostLayoutFragment : RelicFragment() {
    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    private lateinit var preferenceChangedListener: PreferenceChangedListener

    private lateinit var previewPost : PostModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        previewPost = ViewPreferencesHelper.initializePreviewPost(resources)

        return inflater.inflate(R.layout.preferences_post_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postLayoutToolbarView.setOnClickListener { activity!!.onBackPressed() }

        resetPostPreviewView()
    }

    private fun resetPostPreviewView() {
        val postItemView = RelicPostItemView(context!!, postLayout = viewPrefsManager.getPostCardStyle())

        postItemView.setPost(previewPost)

        previewPostLayoutFrameView.apply {
            removeAllViews()
            addView(postItemView)
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