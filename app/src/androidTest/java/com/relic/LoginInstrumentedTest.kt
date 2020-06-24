package com.relic

import android.app.Activity
import android.app.Instrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.runner.AndroidJUnit4
import com.relic.presentation.main.MainActivity
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val LOGIN_ACTIVITY = "com.relic.presentation.login.LoginActivity"

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val intentsTestRule = IntentsTestRule(MainActivity::class.java)

    @Test
    fun loginDialogShownOnInit() {
        onView(withText(R.string.welcome_dialog_title)).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        // verify login activity is created
        intended(allOf(
            hasComponent(LOGIN_ACTIVITY)
        ))
    }

    /*
    This test case represents any scenario where the result code is not ok which should force
    the login dialog to be shown again
     */
    @Test
    fun loginDialogShownWhenLoginActivityCancelled() {
        val result = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        intending(toPackage(LOGIN_ACTIVITY)).respondWith(result)

        onView(withText(R.string.welcome_dialog_title)).check(matches(isDisplayed()))
    }

    /*
    note: we can't yet test for the scenario where the result is "RESULT_OK" because espresso
    intents is currently missing requestCode functionality which our MainActivity relies on to
    determine whether to display the dialog.
     */
}