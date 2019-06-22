package com.relic.presentation.preferences.appearance

import android.content.Context
import android.os.Bundle
import android.support.v7.view.ContextThemeWrapper
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.preferences.PreferenceChangedListener
import com.relic.presentation.preferences.PreferenceLink
import com.relic.preference.PreferencesManager
import com.relic.preference.PreferencesManagerImpl
import kotlinx.android.synthetic.main.preferences_theme.*
import java.util.*

class ThemeFragment : RelicFragment(), AdapterView.OnItemSelectedListener {

    private lateinit var previewPost : PostModel
    private lateinit var rootView : View

    private lateinit var preferenceChangedListener: PreferenceChangedListener

    private lateinit var preferencesManager : PreferencesManager
    private var currentTheme : Int = -1

    // region android lifecycle hooks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        previewPost = initializePreviewPost()

        activity?.let {
            preferencesManager = PreferencesManagerImpl.create(it.getPreferences(Context.MODE_PRIVATE))
            currentTheme = preferencesManager.getApplicationTheme()
        }

        val contextWrapper = ContextThemeWrapper(activity, currentTheme)
        val localInflater = inflater.cloneInContext(contextWrapper)

        return localInflater.inflate(R.layout.preferences_theme, container, false).apply {
            this@ThemeFragment.rootView = this
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        preferencesThemeToolbarView.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        resetPostPreviewView()

        context?.let {
            ArrayAdapter.createFromResource(
                    it,
                    R.array.theme_names,
                    android.R.layout.simple_spinner_item
            ).let { themeDropdownAdapter ->
                themeDropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                selectThemeSpinnerView.apply {
                    adapter = themeDropdownAdapter


                    // we have to do this because spinner auto calls onItemSelected otherwise
                    setSelection(0)
                    onItemSelectedListener = this@ThemeFragment
                }
            }
        }
    }

    // endregion android lifecycle hooks

    // region view helper functions

    private fun initializePreviewPost() : PostModel {
        return PostModel().apply {
            title = resources.getString(R.string.preference_theme_instruction)
            author ="boiledbuns"
            selftext = resources.getString(R.string.long_placeholder_text)
            subreddit = "theme_editor"
            created = Date()
        }
    }

    private fun resetPostPreviewView() {
        val contextWrapper = ContextThemeWrapper(activity, currentTheme)
        val postItemView = RelicPostItemView(contextWrapper)

        postItemView.setPost(previewPost)

        previewPostFrameView.apply {
            removeAllViews()
            addView(postItemView)
        }
    }

    // endregion view helper functions

    // region spinner item selection listener

    override fun onNothingSelected(p0: AdapterView<*>?) { }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val tempTheme = when (pos) {
            1 -> R.style.RelicThemeLight
            else -> R.style.RelicThemePrimary
        }

        // only update if there is a change
        if (currentTheme != tempTheme) {
            currentTheme = tempTheme
            preferencesManager.setApplicationTheme(currentTheme)
            resetPostPreviewView()

            // inform the activity of the changes to the theme
            preferenceChangedListener.onPreferenceChanged(PreferenceLink.Theme)
        }
    }

    // endregion spinner item selection listener

    companion object {

        fun create(listener : PreferenceChangedListener): ThemeFragment {
            return ThemeFragment().apply {
                preferenceChangedListener = listener
            }
        }
    }
}