package com.relic.presentation.subsyncconfig

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.InputType
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.relic.data.PostSource
import com.relic.data.SortScope
import com.relic.data.SortType
import com.relic.presentation.base.RelicPreferenceFragment
import com.relic.scheduler.ScheduleManager
import com.relic.scheduler.SyncRepeatDays
import com.relic.scheduler.SyncRepeatOption
import java.util.*
import javax.inject.Inject


private const val PREFIX_POST_SYNC = "POST_SYNC_"
private const val PREFIX_POST_SYNC_TIME = "POST_SYNC_TIME_"
private const val PREFIX_POST_SYNC_REPEAT = "POST_SYNC_REPEAT_"
private const val PREFIX_POST_SYNC_REPEAT_DAYS = "POST_SYNC_REPEAT_DAYS_"
private const val PREFIX_POST_SYNC_PAGES = "POST_SYNC_PAGES_"
private const val PREFIX_POST_SORT_TYPE = "POST_SORT_TYPE_"
private const val PREFIX_POST_SORT_SCOPE = "POST_SORT_SCOPE_"
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
    private val keyPostSortType by lazy { PREFIX_POST_SORT_TYPE + postSourceName }
    private val keyPostSortScope by lazy { PREFIX_POST_SORT_SCOPE + postSourceName }
    private val keyCommentSync by lazy { PREFIX_COMMENT_SYNC + postSourceName }

    private val sortTypes = SortType.values().map { it.toString().toLowerCase() }.toTypedArray()
    private val sortScopes = SortScope.values().map { it.toString().toLowerCase() }.toTypedArray()

    private val repeatOptions = arrayOf("daily", "weekly")
    private val repeatDays = arrayOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = handleBackPress()
                requireActivity().onBackPressed()
            }
        })
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
            }

            Preference(context).apply {
                key = keyPostSyncTime
                title = "Time to sync: ${postSyncTime()/60}:${postSyncTime()%60} "
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    val calendar: Calendar = Calendar.getInstance()
                    val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        sp.edit().putInt(keyPostSyncTime, hourOfDay*60 + minute).apply()
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
                title = "Repeats: ${postSyncRepeat().toString().toLowerCase()}"
                entries = repeatOptions
                entryValues = repeatOptions

                postSyncCategory.addPreference(this)
                setOnPreferenceChangeListener { preference, newValue ->
                    title = "Repeats: $newValue"
                    true
                }
            }

            DropDownPreference(context).apply {
                key = keyPostSyncRepeatDays
                title = "Repeats on: ${postSyncRepeatDays().toString().toLowerCase()}"
                entries = repeatDays
                entryValues = repeatDays

                postSyncCategory.addPreference(this)
                setOnPreferenceChangeListener { preference, newValue ->
                    title = "Repeats on: $newValue"
                    true
                }
            }

            EditTextPreference(context).apply {
                key = keyPostSyncPages
                title = "Number of pages to sync: ${postSyncRepeatPages()}"

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

        PreferenceCategory(context).let { postSortCategory ->
            postSortCategory.title = "post sync sort preferences"
            screen.addPreference(postSortCategory)

            DropDownPreference(context).apply {
                key = keyPostSortType
                title = "Sort type: ${postSortType().toString().toLowerCase()}"
                entries = sortTypes
                entryValues = sortTypes

                postSortCategory.addPreference(this)
                setOnPreferenceChangeListener { _, newValue ->
                    title = "Sort type: $newValue"
                    true
                }
            }

            DropDownPreference(context).apply {
                key = keyPostSortScope
                title = "Sort scope: ${postSortScope().toString().toLowerCase()}"
                entries = sortScopes
                entryValues = sortScopes

                postSortCategory.addPreference(this)
                setOnPreferenceChangeListener { _, newValue ->
                    title = "Sort scope: $newValue"
                    true
                }
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

    private fun handleSyncPreferenceChange() : Boolean {
        val enabled = isPostSyncEnabled()
        if (enabled) {
            scheduleManager.setupPostSync(
              postSource = postSource,
              pagesToSync = postSyncRepeatPages(),
              timeToSync = postSyncTime(),
              repeatType = postSyncRepeat(),
              repeatDay = postSyncRepeatDays(),
              sortType = postSortType(),
              sortScope = postSortScope(),
              commentSyncEnabled = isCommentSyncEnabled()
            )
        } else {
            scheduleManager.cancelPostSync(postSource)
        }

        // defer back press handle until after we've completed setting up the schedule manager
        return false
    }

    private fun handleBackPress() : Boolean {
        // no need to check for divergence - just update the worker
        handleSyncPreferenceChange()
        return false
    }

    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    // region sub sync preferences manager

    override fun isPostSyncEnabled() : Boolean = sp.getBoolean(keyPostSync, false)

    override fun postSyncTime(): Int = sp.getInt(keyPostSyncTime, 0)

    override fun postSyncRepeat(): SyncRepeatOption {
        return when (sp.getString(keyPostSyncRepeat, null) ) {
            repeatOptions[1] -> SyncRepeatOption.WEEKLY
            else -> SyncRepeatOption.DAILY
        }
    }

    override fun postSyncRepeatDays(): SyncRepeatDays {
        return when (sp.getString(keyPostSyncRepeatDays, null)) {
            repeatDays[6] -> SyncRepeatDays.SUNDAY
            repeatDays[5] -> SyncRepeatDays.SATURDAY
            repeatDays[4] -> SyncRepeatDays.FRIDAY
            repeatDays[3] -> SyncRepeatDays.THURSDAY
            repeatDays[2] -> SyncRepeatDays.WEDNESDAY
            repeatDays[1] -> SyncRepeatDays.TUESDAY
            else -> SyncRepeatDays.MONDAY
        }
    }

    override fun postSyncRepeatPages(): Int = sp.getString(keyPostSyncPages, "0")!!.toInt()

    override fun postSortType(): SortType {
        val sortTypeName = sp.getString(keyPostSortType, "")
        return SortType.values().find { it.toString().toLowerCase() == sortTypeName } ?: SortType.DEFAULT
    }

    override fun postSortScope(): SortScope {
        val sortScopeNames = sp.getString(keyPostSortScope, "")
        return SortScope.values().find { it.toString().toLowerCase() == sortScopeNames } ?: SortScope.NONE
    }

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