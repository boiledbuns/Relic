package com.relic.presentation.preferences.appearance

import android.content.res.Resources
import com.relic.R
import com.relic.domain.models.PostModel
import java.util.*

object ViewPreferencesHelper {
    fun getThemeCode(themeId : Int) {

    }


    fun initializePreviewPost(resources : Resources) : PostModel {
        return PostModel().apply {
            title = resources.getString(R.string.preference_theme_instruction)
            author ="boiledbuns"
            selftext = resources.getString(R.string.long_placeholder_text)
            subreddit = "theme_editor"
            created = Date()
        }
    }
}

