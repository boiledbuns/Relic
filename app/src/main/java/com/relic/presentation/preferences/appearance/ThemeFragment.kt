package com.relic.presentation.preferences.appearance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.presentation.base.RelicFragment

class ThemeFragment : RelicFragment() {

    // region android lifecycle hooks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.preferences_theme, container, false)
    }

    // endregion android lifecycle hooks

    companion object {

        fun create(): ThemeFragment {
            return ThemeFragment()
        }
    }
}