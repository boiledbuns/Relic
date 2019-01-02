package com.relic.presentation.preferences

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.preferences.appearance.ThemeFragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.preferences.*

class PreferencesFragment : RelicFragment() {

    private lateinit var preferenceChangedListener: PreferenceChangedListener

    val preferences : List<PreferenceLink> = listOf(
        PreferenceLink.Appearance,
        PreferenceLink.Theme
    )

    // region android lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            (getParcelable(KEY_PREFERENCE) as PreferenceLink?)?.let { preferenceLink ->
                handleDirectNavigation(preferenceLink)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClicks()
    }

    // endregion android lifecycle hooks

    // region view helper functions

    private fun initOnClicks() {
        preferenceThemeView.setOnClickListener { handleNavigation(PreferenceLink.Theme)}
    }

    // endregion view helper functions

    // region helper functions

    private fun handleDirectNavigation(preferenceLink : PreferenceLink) {
        when (preferenceLink) {

        }
    }

    private fun handleNavigation (preferenceLink : PreferenceLink) {
        val linkFragment : Fragment? = when (preferenceLink) {
            PreferenceLink.Theme -> {
                ThemeFragment.create(preferenceChangedListener)
            }
            else -> null
        }

        linkFragment?.let {
            activity?.supportFragmentManager!!
                .beginTransaction()
                .replace(R.id.perferences_content_frame, it)
                .addToBackStack(TAG)
                .commit()
        }
    }

    // endregion helper functions

    companion object {
        private const val KEY_PREFERENCE = "key_preference"

        fun create(
            preferenceLink : PreferenceLink? = null,
            listener : PreferenceChangedListener
        ) : PreferencesFragment{
            val args = Bundle().apply {
                preferenceLink?.let { putParcelable(KEY_PREFERENCE, preferenceLink)}
            }

            return PreferencesFragment().apply {
                arguments = args
                preferenceChangedListener = listener
            }
        }
    }
}

