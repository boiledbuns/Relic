package com.relic.presentation.preferences.appearance

import android.content.Context
import android.os.Bundle
import android.support.v7.view.ContextThemeWrapper
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.relic.R
import com.relic.data.models.PostModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.customview.RelicPostItemView
import com.relic.util.PreferencesManager
import com.relic.util.PreferencesManagerImpl
import kotlinx.android.synthetic.main.preferences_theme.*

class ThemeFragment : RelicFragment(), AdapterView.OnItemSelectedListener {

    lateinit var previewPost : PostModel
    lateinit var rootView : View

    lateinit var preferencesManager : PreferencesManager

    // region android lifecycle hooks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.let {
            preferencesManager = PreferencesManagerImpl.create(it.getPreferences(Context.MODE_PRIVATE))
        }

        val contextWrapper = ContextThemeWrapper(activity, preferencesManager.getApplicationTheme())
        val localInflater = inflater.cloneInContext(contextWrapper)

        return localInflater.inflate(R.layout.preferences_theme, container, false).apply {
            this@ThemeFragment.rootView = this
            initializePreview()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            ArrayAdapter.createFromResource(
                    it,
                    R.array.theme_names,
                    android.R.layout.simple_spinner_item
            ).let { themeDropdownAdapter ->
                themeDropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                selectThemeSpinnerView.apply {
                    adapter = themeDropdownAdapter
                    onItemSelectedListener = this@ThemeFragment
                }
            }
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
//            setTheme(R.style.ThemeLight)
//            recreate()
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

    // region spinner item selection listener

    override fun onNothingSelected(p0: AdapterView<*>?) { }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val selectedTheme = when (pos) {
            1 -> R.style.RelicThemeLight
            else -> R.style.RelicThemePrimary
        }

        preferencesManager.setApplicationTheme(selectedTheme)
    }

    // endregion spinner item selection listener

    companion object {

        fun create(): ThemeFragment {
            return ThemeFragment()
        }
    }
}