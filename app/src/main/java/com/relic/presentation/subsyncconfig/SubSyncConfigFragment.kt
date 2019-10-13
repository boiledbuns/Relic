package com.relic.presentation.subsyncconfig

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.relic.data.PostSource
import com.relic.presentation.base.RelicPreferenceFragment
import com.relic.scheduler.ScheduleManager
import javax.inject.Inject

private const val PREFIX_POST_SYNC = "POST_SYNC_"
private const val PREFIX_POST_SYNC_PAGES = "POST_SYNC_PAGES_"
private const val PREFIX_COMMENT_SYNC = "COMMENT_SYNC_"

class SubSyncConfigFragment : RelicPreferenceFragment() {

    @Inject
    lateinit var scheduleManager : ScheduleManager

    private lateinit var postSource: PostSource
    private lateinit var postSourceName: String

    private val keyPostSync by lazy { PREFIX_POST_SYNC + postSourceName }
    private val keyPostSyncPages by lazy { PREFIX_POST_SYNC_PAGES + postSourceName }
    private val keyCommentSync by lazy { PREFIX_COMMENT_SYNC + postSourceName }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        arguments?.apply {
            getParcelable<PostSource>(ARG_POST_SOURCE)?.let {
                postSource = it
                postSourceName = it.getSourceName()
            }
        }

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context).apply {
            title = "Sync settings for $postSourceName"
        }

        PreferenceCategory(context).let { postSyncCategory ->
            postSyncCategory.title = "post sync preferences"
            screen.addPreference(postSyncCategory)

            SwitchPreferenceCompat(context).apply {
                key = keyPostSync
                title = "Enable post sync"
                postSyncCategory.addPreference(this)

                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                    handlePostSyncChange(preference, newValue as Boolean)
                }
            }

            EditTextPreference(context).apply {
                key = keyPostSyncPages
                title = "Number of pages to sync"
                setOnBindEditTextListener {
                    it.inputType = InputType.TYPE_CLASS_NUMBER
                }
                postSyncCategory.addPreference(this)
            }
        }

        PreferenceCategory(context).let { commentSyncCategory ->
            commentSyncCategory.title = "comment sync preferences"
            screen.addPreference(commentSyncCategory)

            SwitchPreferenceCompat(context).apply {
                key = keyCommentSync
                title = "Enable comment sync"
                commentSyncCategory.addPreference(this)
            }
        }

        preferenceScreen = screen
    }

    private fun handlePostSyncChange(preference : Preference, enabled : Boolean) : Boolean {
        if (enabled) {
            scheduleManager.setupPostSync(postSource)
        } else {
            scheduleManager.cancelPostSync(postSource)
        }

        // always accept the update
        return true
    }

    companion object {
        const val ARG_POST_SOURCE = "ARG_POST_SOURCE"

        fun create(postSource: PostSource) : SubSyncConfigFragment {
            return SubSyncConfigFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_POST_SOURCE, postSource)
                }
            }
        }
    }
}