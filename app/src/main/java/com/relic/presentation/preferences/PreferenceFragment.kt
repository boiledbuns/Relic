package com.relic.presentation.preferences

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import kotlinx.android.parcel.Parcelize

class PreferenceFragment : Fragment() {

    // region android lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            (getParcelable(KEY_PREFERENCE) as PreferenceLink?)?.let { preferenceLink ->
                navigateDirectlyToLink(preferenceLink)
            }
        }
    }

    // endregion android lifecycle hooks

    // region helper functions

    private fun navigateDirectlyToLink(preferenceLink : PreferenceLink) {
        when (preferenceLink) {

        }
    }

    // endregion helper functions

    companion object {
        private const val KEY_PREFERENCE = "key_preference"

        fun create(preferenceLink : PreferenceLink? = null) : PreferenceFragment{
            val args = Bundle().apply {
                preferenceLink?.let { putParcelable(KEY_PREFERENCE, preferenceLink)}
            }

            return PreferenceFragment().apply {
                arguments = args
            }
        }

        /**
         * used for opening specific preference options
         */
        sealed class PreferenceLink : Parcelable {
            @Parcelize
            object View : PreferenceLink()
        }
    }
}

