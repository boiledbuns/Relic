package com.relic.presentation.preferences

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

interface PreferenceChangedListener {
    fun onPreferenceChanged(preferenceLink : PreferenceLink)
}

/**
 * used for opening specific preference options
 */
sealed class PreferenceLink : Parcelable {
    @Parcelize
    object Appearance : PreferenceLink()
    @Parcelize
    object Theme : PreferenceLink()
    @Parcelize
    object PostLayout : PreferenceLink()
}