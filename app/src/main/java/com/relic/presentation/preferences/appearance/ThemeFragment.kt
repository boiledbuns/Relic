package com.relic.presentation.preferences.appearance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.data.models.PostModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.customview.RelicPostItemView

class ThemeFragment : RelicFragment() {

    lateinit var previewPost : PostModel
    lateinit var rootView : View

    // region android lifecycle hooks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.preferences_theme, container, false).apply {
            this@ThemeFragment.rootView = this
            initializePreview()
        }
    }

    // endregion android lifecycle hooks

    // region view helper functions

    private fun initializePreview() {
        initializePreviewPost()

        rootView.findViewById<RelicPostItemView>(R.id.previewPostView).let { postItemView ->
            postItemView.setPost(previewPost)
        }

        // TODO separate this fragment into its own activity or
        // TODO manually set attributes of the preview post to allow it to reload new theme dynamically
        activity?.apply {
            setTheme(R.style.ThemeLight)
            recreate()
        }
    }

    private fun initializePreviewPost() {
        previewPost = PostModel().apply {
            title = resources.getString(R.string.preference_theme_instruction)
            selftext = resources.getString(R.string.long_placeholder_text)
            subreddit = "theme_editor"
        }
    }

    // endregion view helper functions

    companion object {

        fun create(): ThemeFragment {
            return ThemeFragment()
        }
    }
}