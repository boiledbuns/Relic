package com.relic.presentation.preferences

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.relic.R
import com.relic.util.RequestCodes

class PreferencesActivity : AppCompatActivity(), PreferenceChangedListener  {

    private var changedPreferences = ArrayList<PreferenceLink>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_preferences)

        // retrieve the link to be used
        val preferenceLink : PreferenceLink? = intent.getParcelableExtra(KEY_PREF_LINK)

        PreferencesFragment.create(preferenceLink, this).let { preferencesFragment ->
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.perferences_content_frame, preferencesFragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        if (changedPreferences.isNotEmpty()) {

            Intent().apply {
                putParcelableArrayListExtra(KEY_RESULT_PREF_LINKS, changedPreferences)
                setResult(Activity.RESULT_OK, this)
            }

        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        super.onBackPressed()
    }

    // region call back listener

    override fun onPreferenceChanged(preferenceLink : PreferenceLink) {
        when (preferenceLink) {
            PreferenceLink.Theme -> { changedPreferences.add(PreferenceLink.Theme)}
        }
    }

    // end region call back listener

    companion object {
        private val KEY_PREF_LINK = "key_pref_link"

        val KEY_RESULT_PREF_LINKS = "key_result_pref_link"

        fun startForResult(fromActivity : Activity, preferenceLink : PreferenceLink? = null) {
            Intent(fromActivity, PreferencesActivity::class.java).let { intent ->
                intent.putExtra(KEY_PREF_LINK, preferenceLink)
                fromActivity.startActivityForResult(intent, RequestCodes.CHANGED_PREFERENCES)
            }
        }
    }
}