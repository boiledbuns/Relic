package com.relic

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.relic.presentation.main.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    var intentsRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun loginDialogShown() {
        onView(withText(R.string.welcome_dialog_title)).check(matches(isDisplayed()))
    }
}