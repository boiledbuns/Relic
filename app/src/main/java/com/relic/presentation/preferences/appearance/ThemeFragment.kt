package com.relic.presentation.preferences.appearance

import android.os.Bundle
import androidx.appcompat.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.relic.R
import com.relic.domain.models.PostModel
import com.relic.preference.ViewPreferencesManager
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.customview.RelicPostItemView
import com.relic.presentation.preferences.PreferenceChangedListener
import com.relic.presentation.preferences.PreferenceLink
import kotlinx.android.synthetic.main.preferences_theme.*
import javax.inject.Inject

class ThemeFragment : RelicFragment() {

    @Inject
    lateinit var viewPrefsManager : ViewPreferencesManager

    private lateinit var previewPost : PostModel
    private lateinit var rootView : View

    private lateinit var preferenceChangedListener: PreferenceChangedListener

    private var currentTheme : Int = 0

    // region lifecycle hooks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        previewPost = ViewPreferencesHelper.initializePreviewPost(resources)
        currentTheme = viewPrefsManager.getAppTheme()

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

        val themeDropdownAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.theme_names,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        selectThemeSpinnerView.apply {
            adapter = themeDropdownAdapter

            // we have to do this because spinner auto calls onItemSelected otherwise
            setSelection(getThemePosition(currentTheme))
            onItemSelectedListener = ThemeListener()
        }

        resetPostPreviewView()
    }

    // endregion lifecycle hooks

    private fun resetPostPreviewView() {
        val contextWrapper = ContextThemeWrapper(activity, currentTheme)
        val postItemView = RelicPostItemView(contextWrapper, postLayout = viewPrefsManager.getPostCardStyle())

        postItemView.setPost(previewPost)

        previewPostFrameView.apply {
            removeAllViews()
            addView(postItemView)
        }
    }

    // when we retrieve the selected theme from the preferences manager, we need to use this
    // method to get the position of the theme in the theme spinner
    private fun getThemePosition(themeId: Int) : Int {
        return when(themeId) {
            R.style.RelicThemeLight -> 1
            else -> 0 // position of default theme
        }
    }

    private fun getThemeFromPosition(position : Int) : Int {
        return when (position) {
            1 -> R.style.RelicThemeLight
            else -> R.style.RelicThemePrimary
        }
    }

    private fun handleThemeChange(themePosition : Int) {
        val selectedTheme = getThemeFromPosition(themePosition)

        // only update if there is a change
        if (currentTheme != selectedTheme) {
            currentTheme = selectedTheme
            viewPrefsManager.setAppTheme(currentTheme)

            fragmentManager!!.beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit()

            resetPostPreviewView()

            // inform the activity of the changes to the theme
            preferenceChangedListener.onPreferenceChanged(PreferenceLink.Theme)
        }
    }

    inner class ThemeListener : AdapterView.OnItemSelectedListener{
        override fun onNothingSelected(p0: AdapterView<*>?) { }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            when (parent?.id){
                selectThemeSpinnerView.id -> handleThemeChange(pos)
            }
        }
    }

    companion object {

        fun create(listener : PreferenceChangedListener): ThemeFragment {
            return ThemeFragment().apply {
                preferenceChangedListener = listener
            }
        }
    }
}