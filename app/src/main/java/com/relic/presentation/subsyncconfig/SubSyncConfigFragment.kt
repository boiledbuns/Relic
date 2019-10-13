package com.relic.presentation.subsyncconfig

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.InputType
import androidx.annotation.RequiresApi
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.relic.data.PostSource
import com.relic.presentation.base.RelicPreferenceFragment
import com.relic.scheduler.ScheduleManager
import java.util.*
import javax.inject.Inject


private const val PREFIX_POST_SYNC = "POST_SYNC_"
private const val PREFIX_POST_SYNC_TIME = "POST_SYNC_TIME_"
private const val PREFIX_POST_SYNC_REPEAT = "POST_SYNC_REPEAT_"
private const val PREFIX_POST_SYNC_REPEAT_DAYS = "POST_SYNC_REPEAT_DAYS_"
private const val PREFIX_POST_SYNC_PAGES = "POST_SYNC_PAGES_"
private const val PREFIX_COMMENT_SYNC = "COMMENT_SYNC_"

class SubSyncConfigFragment : RelicPreferenceFragment(), SubSyncPreferenceManager {

    @Inject
    lateinit var scheduleManager : ScheduleManager

    private lateinit var postSource: PostSource
    private lateinit var postSourceName: String

    private val keyPostSync by lazy { PREFIX_POST_SYNC + postSourceName }
    private val keyPostSyncTime by lazy { PREFIX_POST_SYNC_TIME + postSourceName }
    private val keyPostSyncRepeat by lazy { PREFIX_POST_SYNC_REPEAT + postSourceName }
    private val keyPostSyncRepeatDays by lazy { PREFIX_POST_SYNC_REPEAT_DAYS + postSourceName }
    private val keyPostSyncPages by lazy { PREFIX_POST_SYNC_PAGES + postSourceName }
    private val keyCommentSync by lazy { PREFIX_COMMENT_SYNC + postSourceName }

    private val repeatOptions = arrayOf("daily", "weekly")
    private val repeatDays = arrayOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.N)
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
                    handlePostSyncChange(newValue as Boolean)
                }
            }

            Preference(context).apply {
                key = keyPostSyncTime
                title = "Time to sync: ${sp.getInt(keyPostSyncTime, 0)}"
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    val calendar: Calendar = Calendar.getInstance()
                    val listener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        handlePostSyncTimeChanged(hourOfDay, minute)
                    }

                    // get previously set times if any, current time otherwise
                    val timeInMin = sp.getInt(keyPostSyncTime, -1)
                    val timeOfDay = if (timeInMin > 0) timeInMin/60 else calendar.get(Calendar.HOUR)
                    val minute = if (timeInMin > 0) timeInMin%60 else calendar.get(Calendar.MINUTE)

                    TimePickerDialog(context, listener, timeOfDay, minute, false).show()
                    true
                }
                postSyncCategory.addPreference(this)
            }

            DropDownPreference(context).apply {
                key = keyPostSyncRepeat
                title = "Repeats: ${sp.getString(keyPostSyncRepeat, "")}"
                entries = repeatOptions
                entryValues = repeatOptions
                postSyncCategory.addPreference(this)
            }

            DropDownPreference(context).apply {
                key = keyPostSyncRepeatDays
                title = "Repeats on: ${sp.getString(keyPostSyncRepeatDays, "")}"
                entries = repeatDays
                entryValues = repeatDays
                postSyncCategory.addPreference(this)
            }

            EditTextPreference(context).apply {
                key = keyPostSyncPages
                title = "Number of pages to sync: ${sp.getString(keyPostSyncPages, "0")!!.toInt()}"

                setOnBindEditTextListener {
                    it.inputType = InputType.TYPE_CLASS_NUMBER
                }

                setOnPreferenceChangeListener { _, newValue ->
                    title = "Number of pages to sync: $newValue"
                    true
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

    private fun handlePostSyncChange(enabled : Boolean) : Boolean {
        if (enabled) {
            val pagesToSync = preferenceManager.sharedPreferences.getInt(keyPostSyncPages, 0)
            scheduleManager.setupPostSync(postSource, pagesToSync, 0L)
        } else {
            scheduleManager.cancelPostSync(postSource)
        }

        // always accept the update
        return true
    }

    private fun handlePostSyncTimeChanged(hourOfDay : Int, minute : Int) {
        // convert to minutes
        sp.edit().putInt(keyPostSyncTime, hourOfDay*60 + minute).apply()
    }

    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    // region sub sync preferences manager

    override fun isPostSyncEnabled() : Boolean=  sp.getBoolean(keyPostSync, false)
    override fun postSyncTime(): String? =  sp.getString(keyPostSyncTime, null)
    override fun postSyncRepeat(): String? = sp.getString(keyPostSyncRepeat, null)
    override fun postSyncRepeatDays(): String? = sp.getString(keyPostSyncRepeatDays, null)
    override fun postSyncRepeatPages(): Int = sp.getInt(keyPostSyncPages, 0)
    override fun isCommentSyncEnabled(): Boolean = sp.getBoolean(keyCommentSync, false)

    // endregion sub sync preferences manager

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